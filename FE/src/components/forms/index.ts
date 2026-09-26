/**
 * ============================================================================
 * FILE: index.ts (Barrel Export)
 * MỤC ĐÍCH: 
 *   - Gom nhóm và xuất (export) toàn bộ các khung form nhập liệu và file điều hướng.
 *   - Giúp đội Frontend có thể import dễ dàng chỉ bằng 1 dòng lệnh:
 *       import { FormsNavigation, HorseInputForm, IncidentReportForm } from "@/components/forms";
 * ============================================================================
 */

export * from "./HorseInputForm";
export * from "./SupplyInputForm";
export * from "./UserInputForm";
export * from "./FinanceInputForm";
export * from "./IncidentReportForm";
export * from "./CareTaskForm";
export * from "./FormsNavigation";
