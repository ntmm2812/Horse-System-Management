import { createContext, useContext, useState, useEffect, type ReactNode } from "react";
import { toast } from "sonner";
import { useAuth } from "./auth-context";

export interface StableHorse {
  stall: string;
  name: string;
  status: "Khỏe" | "Theo dõi" | "Chấn thương";
  tone: "healthy" | "watch" | "injured";
  isLocked?: boolean;
  injuryDetail?: string;
  injurySeverity?: "Nhẹ" | "Trung bình" | "Nặng";
  treatmentNotes?: string;
  heartRate?: number;
}

export interface SystemNotification {
  id: string;
  type: "danger" | "warn" | "info" | "success";
  title: string;
  desc: string;
  time: string;
  isRead: boolean;
}

export interface AuditLogEntry {
  id: string;
  time: string;
  actor: string;
  action: string;
}

export interface ScheduleItem {
  id: string;
  time: string;
  horse: string;
  exercise: string;
  track: string;
  status: "Hoàn thành" | "Đang tập" | "Sắp tới" | "Đã hủy";
}

const initialStableHorses: StableHorse[] = [
  { stall: "A01", name: "Xích Thố", status: "Khỏe", tone: "healthy", isLocked: false, heartRate: 142 },
  { stall: "A02", name: "Black Caviar", status: "Theo dõi", tone: "watch", isLocked: false, injuryDetail: "Vận tốc giảm 12% ở vòng 3", injurySeverity: "Nhẹ", heartRate: 158 },
  { stall: "A03", name: "Sao Mai", status: "Khỏe", tone: "healthy", isLocked: false, heartRate: 138 },
  { stall: "A04", name: "Hắc Phong", status: "Chấn thương", tone: "injured", isLocked: true, injuryDetail: "Chân trước trái — gân gấp", injurySeverity: "Trung bình", treatmentNotes: "Chườm lạnh 3 lần/ngày, nghỉ tập 7 ngày. Tái khám siêu âm vào 28/09.", heartRate: 196 },
  { stall: "B01", name: "Bạch Long", status: "Khỏe", tone: "healthy", isLocked: false, heartRate: 140 },
  { stall: "B02", name: "Thiên Mã", status: "Theo dõi", tone: "watch", isLocked: false, injuryDetail: "Nhiệt độ cơ thể 39,1°C", injurySeverity: "Nhẹ", heartRate: 162 },
  { stall: "B03", name: "Hoàng Vũ", status: "Khỏe", tone: "healthy", isLocked: false, heartRate: 144 },
  { stall: "B04", name: "Vệt Nắng", status: "Khỏe", tone: "healthy", isLocked: false, heartRate: 136 },
];

const initialSchedules: ScheduleItem[] = [
  { id: "1", time: "06:00", horse: "Xích Thố", exercise: "Chạy bền 2.400m", track: "Sân cỏ", status: "Hoàn thành" },
  { id: "2", time: "07:15", horse: "Black Caviar", exercise: "Tăng tốc 1.600m", track: "Sân cát", status: "Đang tập" },
  { id: "3", time: "09:00", horse: "Sao Mai", exercise: "Phản xạ xuất phát", track: "Cổng số 3", status: "Sắp tới" },
  { id: "4", time: "15:30", horse: "Bạch Long", exercise: "Hồi phục chủ động", track: "Hồ bơi", status: "Sắp tới" },
];

interface StableContextType {
  horses: StableHorse[];
  setHorses: React.Dispatch<React.SetStateAction<StableHorse[]>>;
  schedules: ScheduleItem[];
  setSchedules: React.Dispatch<React.SetStateAction<ScheduleItem[]>>;
  notifications: SystemNotification[];
  auditLogs: AuditLogEntry[];
  addAuditLog: (action: string) => void;
  addNotification: (title: string, desc: string, type?: "danger" | "warn" | "info" | "success") => void;
  markAllNotificationsRead: () => void;
  clearNotifications: () => void;
  toggleHorseLock: (horseName: string) => void;
  updateHorseMedical: (
    horseName: string,
    injuryDetail: string,
    severity: "Nhẹ" | "Trung bình" | "Nặng",
    notes: string
  ) => void;
}

const StableContext = createContext<StableContextType | undefined>(undefined);

export function StableProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const [horses, setHorses] = useState<StableHorse[]>(initialStableHorses);
  const [schedules, setSchedules] = useState<ScheduleItem[]>(initialSchedules);
  const [notifications, setNotifications] = useState<SystemNotification[]>([
    { id: "1", type: "danger", title: "Cảnh báo nhịp tim vượt ngưỡng", desc: "Hắc Phong đạt 196 bpm (vượt ngưỡng 180 bpm).", time: "2 phút trước", isRead: false },
    { id: "2", type: "warn", title: "Giảm vận tốc bất thường", desc: "Black Caviar giảm 12% vận tốc ở vòng chạy thứ 3.", time: "8 phút trước", isRead: false },
    { id: "3", type: "warn", title: "Nhiệt độ cơ thể cao", desc: "Thiên Mã ghi nhận thân nhiệt 39,1°C.", time: "15 phút trước", isRead: false },
    { id: "4", type: "success", title: "Hoàn tất ca chăm sóc sáng", desc: "Trần Minh Khoa đã hoàn tất vệ sinh dãy chuồng A.", time: "45 phút trước", isRead: true },
  ]);
  const [auditLogs, setAuditLogs] = useState<AuditLogEntry[]>([
    { id: "1", time: "09:42", actor: "BS. Lê Thu Hà", action: "Cập nhật hồ sơ y tế & khóa lịch tập của Hắc Phong" },
    { id: "2", time: "09:31", actor: "Nguyễn Văn A", action: "Đánh giá phong độ và giáo án bứt tốc Black Caviar" },
    { id: "3", time: "08:55", actor: "Trần Minh Khoa", action: "Hoàn thành vệ sinh và cho ăn cữ sáng khu chuồng A" },
    { id: "4", time: "08:10", actor: "Phạm Ngọc Lan", action: "Duyệt phiếu nhập cỏ Timothy & yến mạch #VT-0922" },
  ]);

  const addAuditLog = (action: string) => {
    const now = new Date();
    const timeStr = `${String(now.getHours()).padStart(2, "0")}:${String(now.getMinutes()).padStart(2, "0")}`;
    const newEntry: AuditLogEntry = {
      id: `log-${Date.now()}`,
      time: timeStr,
      actor: user?.name || "Người dùng",
      action,
    };
    setAuditLogs((prev) => [newEntry, ...prev]);
  };

  const addNotification = (
    title: string,
    desc: string,
    type: "danger" | "warn" | "info" | "success" = "info"
  ) => {
    const newNotif: SystemNotification = {
      id: `notif-${Date.now()}`,
      type,
      title,
      desc,
      time: "Vừa xong",
      isRead: false,
    };
    setNotifications((prev) => [newNotif, ...prev]);
  };

  const markAllNotificationsRead = () => {
    setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
    toast.success("Đã đánh dấu tất cả thông báo là đã đọc!");
  };

  const clearNotifications = () => {
    setNotifications([]);
    toast.info("Đã xóa toàn bộ thông báo.");
  };

  const toggleHorseLock = (horseName: string) => {
    let nextLockedState = false;
    setHorses((prev) =>
      prev.map((h) => {
        if (h.name === horseName) {
          nextLockedState = !h.isLocked;
          return {
            ...h,
            isLocked: nextLockedState,
            status: nextLockedState ? "Chấn thương" : "Khỏe",
            tone: nextLockedState ? "injured" : "healthy",
          };
        }
        return h;
      })
    );

    if (nextLockedState) {
      addAuditLog(`KHÓA HUẤN LUYỆN khẩn cấp cho chiến mã ${horseName}`);
      addNotification(
        "Khóa huấn luyện khẩn cấp",
        `Bác sĩ đã khóa lịch tập của ${horseName}. HLV Trưởng cần điều chỉnh giáo án.`,
        "danger"
      );
      toast.error(`ĐÃ KHÓA HUẤN LUYỆN cho ${horseName}!`, {
        description: "Lịch tập của chiến mã này đã bị vô hiệu hóa.",
      });
    } else {
      addAuditLog(`MỞ KHÓA huấn luyện cho chiến mã ${horseName}`);
      addNotification("Mở khóa huấn luyện", `${horseName} đã bình phục và sẵn sàng tập luyện trở lại.`, "success");
      toast.success(`Đã mở khóa huấn luyện cho ${horseName}!`);
    }
  };

  const updateHorseMedical = (
    horseName: string,
    injuryDetail: string,
    severity: "Nhẹ" | "Trung bình" | "Nặng",
    notes: string
  ) => {
    setHorses((prev) =>
      prev.map((h) =>
        h.name === horseName
          ? {
              ...h,
              injuryDetail,
              injurySeverity: severity,
              treatmentNotes: notes,
            }
          : h
      )
    );
    addAuditLog(`Cập nhật hồ sơ bệnh án ${horseName}: ${injuryDetail} (${severity})`);
    toast.success(`Đã cập nhật hồ sơ y tế cho ${horseName}!`);
  };

  return (
    <StableContext.Provider
      value={{
        horses,
        setHorses,
        schedules,
        setSchedules,
        notifications,
        auditLogs,
        addAuditLog,
        addNotification,
        markAllNotificationsRead,
        clearNotifications,
        toggleHorseLock,
        updateHorseMedical,
      }}
    >
      {children}
    </StableContext.Provider>
  );
}

export function useStable() {
  const context = useContext(StableContext);
  if (!context) {
    throw new Error("useStable must be used within a StableProvider");
  }
  return context;
}
