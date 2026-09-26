package com.horsemanagement;

import com.horsemanagement.dao.support.Database;

import com.fasterxml.jackson.databind.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.*;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts={"/schema-test.sql","/data-test.sql"})
class ApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired Database db;
    @Autowired PasswordEncoder passwords;
    private static final String PASSWORD="TestPassword!2026";
    private String admin,groom,other;

    @BeforeEach void accounts() throws Exception {
        db.update("UPDATE dbo.[USER] SET [Password]=?",passwords.encode(PASSWORD));
        admin=login("admin@test.example");groom=login("groom@test.example");other=login("other@test.example");
    }
    private String login(String email) throws Exception {
        return json.readTree(mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(Map.of("email",email,"password",PASSWORD))))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("accessToken").asText();
    }
    private String body(Object value) throws Exception {return json.writeValueAsString(value);}
    private String auth(String token) {return "Bearer "+token;}

    @Test void rolesTokensAndLogoutAreEnforced() throws Exception {
        mvc.perform(get("/api/manager/horses")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/manager/horses").header("Authorization",auth(groom))).andExpect(status().isForbidden());
        mvc.perform(get("/api/groom/tasks").header("Authorization",auth(admin))).andExpect(status().isForbidden());
        mvc.perform(get("/api/auth/me").header("Authorization",auth(admin)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("Admin")).andExpect(jsonPath("$.password").doesNotExist());
        assertThat(db.list("SELECT TokenHash FROM dbo.ApiToken").toString()).doesNotContain(admin);
        mvc.perform(post("/api/auth/logout").header("Authorization",auth(groom))).andExpect(status().isNoContent());
        mvc.perform(get("/api/groom/tasks").header("Authorization",auth(groom))).andExpect(status().isUnauthorized());
    }
    @Test void registrationNeedsApprovalAndCannotChoosePrivilegedRole() throws Exception {
        var registration=new HashMap<String,Object>(Map.of("fullName","New Owner","email","new@test.example","password",PASSWORD));
        registration.put("roleId",1);
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body(registration))).andExpect(status().isBadRequest());
        registration.remove("roleId");
        int id=json.readTree(mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body(registration)))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("userId").asInt();
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content(body(Map.of("email","new@test.example","password",PASSWORD)))).andExpect(status().isForbidden());
        mvc.perform(post("/api/manager/users/"+id+"/approval").header("Authorization",auth(admin))
            .contentType(MediaType.APPLICATION_JSON).content("{\"approved\":true}")).andExpect(status().isOk());
        String token=login("new@test.example");
        mvc.perform(get("/api/manager/horses").header("Authorization",auth(token))).andExpect(status().isForbidden());
        mvc.perform(post("/api/manager/users/"+id+"/approval").header("Authorization",auth(admin))
            .contentType(MediaType.APPLICATION_JSON).content("{\"approved\":true}")).andExpect(status().isConflict());
    }
    @Test void employeeManagementHashesPasswordsRevokesAccessAndProtectsAdmin() throws Exception {
        int id=json.readTree(mvc.perform(post("/api/manager/users").header("Authorization",auth(admin))
            .contentType(MediaType.APPLICATION_JSON).content(body(Map.of("fullName","New Groom","email","newgroom@test.example","roleId",5,"password",PASSWORD))))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.password").doesNotExist())
            .andReturn().getResponse().getContentAsString()).get("userId").asInt();
        String hash=(String)db.one("SELECT [Password] AS password FROM dbo.[USER] WHERE UserID=?",id).get("password");
        assertThat(hash).isNotEqualTo(PASSWORD);assertThat(passwords.matches(PASSWORD,hash)).isTrue();
        String token=login("newgroom@test.example");
        mvc.perform(patch("/api/manager/users/"+id+"/status").header("Authorization",auth(admin)).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"DISABLED\"}"))
            .andExpect(status().isOk());
        mvc.perform(get("/api/groom/tasks").header("Authorization",auth(token))).andExpect(status().isUnauthorized());
        mvc.perform(patch("/api/manager/users/1/status").header("Authorization",auth(admin)).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"DISABLED\"}"))
            .andExpect(status().isConflict());
        mvc.perform(get("/api/manager/users").header("Authorization",auth(admin))).andExpect(status().isOk()).andExpect(jsonPath("$.total").value(7));
    }
    @Test void permissionChangesApplyImmediatelyAndDoNotCrossRoleBoundary() throws Exception {
        mvc.perform(put("/api/manager/roles/5/permissions").header("Authorization",auth(admin))
            .contentType(MediaType.APPLICATION_JSON).content("{\"permissionIds\":[3]}" )).andExpect(status().isOk());
        mvc.perform(get("/api/groom/tasks").header("Authorization",auth(groom))).andExpect(status().isForbidden());
        mvc.perform(get("/api/manager/horses").header("Authorization",auth(groom))).andExpect(status().isForbidden());
        mvc.perform(put("/api/manager/roles/1/permissions").header("Authorization",auth(admin))
            .contentType(MediaType.APPLICATION_JSON).content("{\"permissionIds\":[]}" )).andExpect(status().isConflict());
    }
    @Test void horseValidationAndArchivePreserveHistory() throws Exception {
        var horse=new HashMap<String,Object>(Map.of("name","New Horse","gender","Male","age",4,"weight",450,"ownerId",4));
        horse.put("weight",-1);
        mvc.perform(post("/api/manager/horses").header("Authorization",auth(admin)).contentType(MediaType.APPLICATION_JSON).content(body(horse))).andExpect(status().isBadRequest());
        horse.put("weight",450);horse.put("ownerId",2);
        mvc.perform(post("/api/manager/horses").header("Authorization",auth(admin)).contentType(MediaType.APPLICATION_JSON).content(body(horse))).andExpect(status().isBadRequest());
        horse.put("ownerId",4);
        mvc.perform(post("/api/manager/horses").header("Authorization",auth(admin)).contentType(MediaType.APPLICATION_JSON).content(body(horse))).andExpect(status().isCreated());
        mvc.perform(delete("/api/manager/horses/1").header("Authorization",auth(admin))).andExpect(status().isNoContent());
        mvc.perform(get("/api/manager/horses/1").header("Authorization",auth(admin))).andExpect(status().isNotFound());
        assertThat(db.count("SELECT COUNT(*) FROM dbo.TRAININGSESSION WHERE HorseID=1")).isEqualTo(1);
    }
    @Test void inventoryRejectsNegativeStockAndStaleWrites() throws Exception {
        mvc.perform(post("/api/manager/supplies/1/adjustments").header("Authorization",auth(admin))
            .contentType(MediaType.APPLICATION_JSON).content("{\"delta\":-21,\"version\":0,\"reason\":\"Use\"}" )).andExpect(status().isConflict());
        mvc.perform(post("/api/manager/supplies/1/adjustments").header("Authorization",auth(admin))
            .contentType(MediaType.APPLICATION_JSON).content("{\"delta\":-5,\"version\":0,\"reason\":\"Use\"}" ))
            .andExpect(status().isOk()).andExpect(jsonPath("$.quantityInStock").value(15)).andExpect(jsonPath("$.version").value(1));
        mvc.perform(post("/api/manager/supplies/1/adjustments").header("Authorization",auth(admin))
            .contentType(MediaType.APPLICATION_JSON).content("{\"delta\":10,\"version\":0,\"reason\":\"Restock\"}" )).andExpect(status().isConflict());
        mvc.perform(get("/api/groom/supplies").header("Authorization",auth(groom))).andExpect(status().isOk()).andExpect(jsonPath("$.items[0].quantityInStock").value(15));
    }
    @Test void groomOnlySeesAssignedHorsesAndCompletesOwnPastOrCurrentTasks() throws Exception {
        mvc.perform(get("/api/groom/stables").header("Authorization",auth(groom))).andExpect(status().isOk()).andExpect(jsonPath("$.total").value(1)).andExpect(jsonPath("$.items[0].horseId").value(1));
        mvc.perform(get("/api/groom/horses/2/feeding-plan").header("Authorization",auth(groom))).andExpect(status().isNotFound());
        mvc.perform(get("/api/groom/horses/1/feeding-plan").header("Authorization",auth(groom))).andExpect(status().isOk());
        mvc.perform(post("/api/groom/tasks/2/complete").header("Authorization",auth(groom))).andExpect(status().isNotFound());
        mvc.perform(post("/api/groom/tasks/3/complete").header("Authorization",auth(groom))).andExpect(status().isConflict());
        mvc.perform(post("/api/groom/tasks/1/complete").header("Authorization",auth(groom)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.horseId").value(1))
            .andExpect(jsonPath("$.taskType").value("Clean stable"))
            .andExpect(jsonPath("$.date").value(LocalDate.now().toString()))
            .andExpect(jsonPath("$.completed").value(true));
        mvc.perform(post("/api/groom/tasks/1/complete").header("Authorization",auth(groom))).andExpect(status().isOk());
        assertThat(db.count("SELECT COUNT(*) FROM dbo.AuditLog WHERE [Action]='COMPLETE' AND TargetID=1")).isEqualTo(1);
    }
    @Test void incidentAndAttachmentEnforceAssignmentOwnershipAndImageFormat() throws Exception {
        var input=new HashMap<String,Object>(Map.of("horseId",2,"incidentType","Broken water trough","date",LocalDate.now().toString()));
        mvc.perform(post("/api/groom/incidents").header("Authorization",auth(groom)).contentType(MediaType.APPLICATION_JSON).content(body(input))).andExpect(status().isNotFound());
        input.put("horseId",1);
        int id=json.readTree(mvc.perform(post("/api/groom/incidents").header("Authorization",auth(groom)).contentType(MediaType.APPLICATION_JSON).content(body(input)))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("reportId").asInt();
        mvc.perform(get("/api/groom/incidents/"+id).header("Authorization",auth(other))).andExpect(status().isNotFound());
        var fake=new MockMultipartFile("file","image.png","image/png","not an image".getBytes());
        mvc.perform(multipart("/api/groom/incidents/"+id+"/image").file(fake).header("Authorization",auth(groom))).andExpect(status().isBadRequest());
        var output=new ByteArrayOutputStream();ImageIO.write(new BufferedImage(2,2,BufferedImage.TYPE_INT_RGB),"png",output);
        var image=new MockMultipartFile("file","../../image.png","image/png",output.toByteArray());
        mvc.perform(multipart("/api/groom/incidents/"+id+"/image").file(image).header("Authorization",auth(groom))).andExpect(status().isNoContent());
        mvc.perform(get("/api/groom/incidents/"+id).header("Authorization",auth(groom)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.imagePath").doesNotExist())
            .andExpect(jsonPath("$.imageUrl").value("/api/groom/incidents/"+id+"/image"))
            .andExpect(jsonPath("$.groomId").value(2)).andExpect(jsonPath("$.horseName").value("Horse A"));
        mvc.perform(get("/api/manager/incidents/"+id).header("Authorization",auth(admin)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.imageUrl").value("/api/manager/incidents/"+id+"/image"));
        mvc.perform(get("/api/groom/incidents").header("Authorization",auth(groom)))
            .andExpect(jsonPath("$.items[0].groomId").value(2)).andExpect(jsonPath("$.items[0].imagePath").doesNotExist());
        var oversized=new MockMultipartFile("file","large.png","image/png",new byte[5*1024*1024+1]);
        // Báo cáo mới chưa có ảnh để kiểm tra riêng giới hạn kích thước.
        int newId=json.readTree(mvc.perform(post("/api/groom/incidents").header("Authorization",auth(groom))
            .contentType(MediaType.APPLICATION_JSON).content(body(input))).andReturn().getResponse().getContentAsString()).get("reportId").asInt();
        mvc.perform(multipart("/api/groom/incidents/"+newId+"/image").file(oversized).header("Authorization",auth(groom)))
            .andExpect(status().isPayloadTooLarge()).andExpect(jsonPath("$.code").value("PAYLOAD_TOO_LARGE"));
        mvc.perform(get("/api/groom/incidents/"+id+"/image").header("Authorization",auth(other))).andExpect(status().isNotFound());
        mvc.perform(get("/api/manager/incidents/"+id+"/image").header("Authorization",auth(admin))).andExpect(status().isOk()).andExpect(content().contentType(MediaType.IMAGE_PNG));
    }
    @Test void financeAndPerformanceReportsUseStoredDataAndValidateDates() throws Exception {
        String today=LocalDate.now().toString();
        for(var entry:List.of(Map.of("entryType","INCOME","amount",1000,"category","Fee","entryDate",today),Map.of("entryType","EXPENSE","amount",250,"category","Feed","entryDate",today)))
            mvc.perform(post("/api/manager/finance-entries").header("Authorization",auth(admin)).contentType(MediaType.APPLICATION_JSON).content(body(entry))).andExpect(status().isCreated());
        mvc.perform(get("/api/manager/reports/finance").param("from",today).param("to",today).header("Authorization",auth(admin)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.income").value(1000)).andExpect(jsonPath("$.expense").value(250)).andExpect(jsonPath("$.net").value(750));
        mvc.perform(get("/api/manager/reports/performance").param("from",today).param("to",today).header("Authorization",auth(admin)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].sessionCount").value(1));
        mvc.perform(get("/api/manager/reports/finance").param("from","2026-12-31").param("to","2026-01-01").header("Authorization",auth(admin))).andExpect(status().isBadRequest());
        mvc.perform(get("/api/manager/audit-logs").header("Authorization",auth(admin))).andExpect(status().isOk());
    }
    @Test void groomReadsOnlyAssignedDayAndCannotWriteInventory() throws Exception {
        mvc.perform(get("/api/groom/stables").header("Authorization",auth(groom)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].stallNumber").value("A-01"))
            .andExpect(jsonPath("$.items[0].dailyRoutine").value("Daily care"));
        mvc.perform(get("/api/groom/schedule").header("Authorization",auth(groom)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].horseId").value(1)).andExpect(jsonPath("$[0].time").value("08:00:00"));
        mvc.perform(get("/api/groom/horses/1/feeding-plan").header("Authorization",auth(groom)))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].grain").value(1.2))
            .andExpect(jsonPath("$[0].meal").value("Morning"));
        String unassignedDay=LocalDate.now().minusDays(7).toString();
        mvc.perform(get("/api/groom/stables").param("date",unassignedDay).header("Authorization",auth(groom)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(0));
        mvc.perform(get("/api/groom/horses/1/feeding-plan").param("date",unassignedDay).header("Authorization",auth(groom)))
            .andExpect(status().isNotFound());
        mvc.perform(post("/api/manager/supplies/1/adjustments").header("Authorization",auth(groom))
            .contentType(MediaType.APPLICATION_JSON).content("{\"delta\":1,\"version\":0,\"reason\":\"Denied\"}"))
            .andExpect(status().isForbidden());
        assertThat(db.count("SELECT QuantityInStock FROM dbo.Supply WHERE SupplyID=1")).isEqualTo(20);
    }

    @Test void managerUpdatesHorseArchivesSupplyAndCanFilterAuditTrail() throws Exception {
        mvc.perform(put("/api/manager/horses/1").header("Authorization",auth(admin))
            .contentType(MediaType.APPLICATION_JSON)
            .content(body(Map.of("name","Updated horse","gender","Male","age",6,"weight",460,"ownerId",4))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Updated horse"));
        int supplyId=json.readTree(mvc.perform(post("/api/manager/supplies").header("Authorization",auth(admin))
            .contentType(MediaType.APPLICATION_JSON)
            .content(body(Map.of("itemName","Brush","type","Tool","quantityInStock",3,"managedBy",2))))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("supplyId").asInt();
        mvc.perform(put("/api/manager/supplies/"+supplyId).header("Authorization",auth(admin))
            .contentType(MediaType.APPLICATION_JSON)
            .content(body(Map.of("itemName","Horse brush","type","Tool","managedBy",2,"version",0))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.version").value(1));
        mvc.perform(delete("/api/manager/supplies/"+supplyId).param("version","0").header("Authorization",auth(admin)))
            .andExpect(status().isConflict());
        mvc.perform(delete("/api/manager/supplies/"+supplyId).param("version","1").header("Authorization",auth(admin)))
            .andExpect(status().isNoContent());
        mvc.perform(get("/api/groom/supplies").param("search","brush").header("Authorization",auth(groom)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(0));
        mvc.perform(get("/api/manager/audit-logs").param("table","Supply").param("userId","1")
            .header("Authorization",auth(admin)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(3))
            .andExpect(jsonPath("$.items[0].action").value("ARCHIVE"));
        assertThat(db.count("SELECT COUNT(*) FROM dbo.Supply WHERE SupplyID=?",supplyId)).isEqualTo(1);
    }

    @Test void rejectedRegistrationCannotLoginOrBeApprovedAgain() throws Exception {
        int id=json.readTree(mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
            .content(body(Map.of("fullName","Rejected Owner","email","rejected@test.example","password",PASSWORD))))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("userId").asInt();
        mvc.perform(post("/api/manager/users/"+id+"/approval").header("Authorization",auth(admin))
            .contentType(MediaType.APPLICATION_JSON).content("{\"approved\":false}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REJECTED"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content(body(Map.of("email","rejected@test.example","password",PASSWORD))))
            .andExpect(status().isForbidden());
        assertThat(db.count("SELECT COUNT(*) FROM dbo.ApiToken WHERE UserID=?",id)).isZero();
        mvc.perform(post("/api/manager/users/"+id+"/approval").header("Authorization",auth(admin))
            .contentType(MediaType.APPLICATION_JSON).content("{\"approved\":true}"))
            .andExpect(status().isConflict());
    }

    @Test void passwordChangeAndExpiredTokensCannotKeepAccess() throws Exception {
        mvc.perform(put("/api/auth/password").header("Authorization",auth(groom)).contentType(MediaType.APPLICATION_JSON)
            .content(body(Map.of("currentPassword",PASSWORD,"newPassword","ChangedPassword!2026")))).andExpect(status().isNoContent());
        mvc.perform(get("/api/auth/me").header("Authorization",auth(groom))).andExpect(status().isUnauthorized());
        db.update("UPDATE dbo.ApiToken SET ExpiresAt=DATEADD(DAY,-1,CURRENT_TIMESTAMP)");
        mvc.perform(get("/api/auth/me").header("Authorization",auth(admin))).andExpect(status().isUnauthorized());
    }

    @Test void loginProfileMatchesMeAndDoesNotAcceptClientChosenRole() throws Exception {
        var response=json.readTree(mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content(body(Map.of("email","admin@test.example","password",PASSWORD))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.user.role").value("Admin"))
            .andReturn().getResponse().getContentAsString());
        var me=json.readTree(mvc.perform(get("/api/auth/me").header("Authorization",auth(response.get("accessToken").asText())))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(response.get("user")).isEqualTo(me);
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content(body(Map.of("email","groom@test.example","password",PASSWORD,"role","Admin"))))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test void paginatedUserFiltersAndTemporalFieldsMatchTheContract() throws Exception {
        mvc.perform(get("/api/manager/users").param("role","Groom").param("status","ACTIVE").param("size","1")
            .header("Authorization",auth(admin))).andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(2)).andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.page").value(0)).andExpect(jsonPath("$.size").value(1))
            .andExpect(jsonPath("$.items[0].role").value("Groom"));
        mvc.perform(get("/api/manager/users").param("search","no match").header("Authorization",auth(admin)))
            .andExpect(jsonPath("$.items").isEmpty()).andExpect(jsonPath("$.totalPages").value(0));
        for(var query:List.of(Map.of("role","manager"),Map.of("status","typo"),Map.of("size","101"))) {
            var request=get("/api/manager/users").header("Authorization",auth(admin));
            query.forEach(request::param);
            mvc.perform(request).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        }
        mvc.perform(get("/api/groom/schedule").header("Authorization",auth(groom)))
            .andExpect(jsonPath("$[0].date").value(LocalDate.now().toString()))
            .andExpect(jsonPath("$[0].time").value("08:00:00"));
        mvc.perform(get("/api/manager/audit-logs").header("Authorization",auth(admin)))
            .andExpect(jsonPath("$.items[0].timestamp").isString());
    }

    @Test void securityAndMvcErrorsShareProblemDetails() throws Exception {
        mvc.perform(get("/api/manager/horses"))
            .andExpect(status().isUnauthorized()).andExpect(header().string("WWW-Authenticate","Bearer"))
            .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
            .andExpect(jsonPath("$.type").value("about:blank")).andExpect(jsonPath("$.title").value("Unauthorized"))
            .andExpect(jsonPath("$.instance").value("/api/manager/horses"))
            .andExpect(jsonPath("$.code").value("UNAUTHENTICATED")).andExpect(jsonPath("$.errors").isArray());
        mvc.perform(get("/api/manager/horses").header("Authorization",auth(groom)))
            .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"))
            .andExpect(jsonPath("$.errors").isArray());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
            .andExpect(jsonPath("$.errors[0].field").isString());
        mvc.perform(get("/api/groom/tasks").param("date","invalid").header("Authorization",auth(groom)))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        mvc.perform(get("/api/manager/horses/999").header("Authorization",auth(admin)))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
