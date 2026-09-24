import {
  Activity,
  BarChart3,
  Bell,
  CalendarDays,
  CircleDollarSign,
  ClipboardCheck,
  Dumbbell,
  HeartPulse,
  Home,
  LayoutDashboard,
  Stethoscope,
  Trophy,
  Users,
  Utensils,
  type LucideIcon,
} from "lucide-react";

export type Role = "trainer" | "vet" | "groom" | "owner" | "manager";
export type RolePath = `/${Role}`;

interface RoleSection {
  id: string;
  label: string;
  icon: LucideIcon;
}

interface RolePage {
  path: RolePath;
  label: string;
  description: string;
  icon: LucideIcon;
  sections: readonly RoleSection[];
}

const overview = { id: "overview", label: "Tổng quan", icon: LayoutDashboard };

export const ROLE_PAGES: Record<Role, RolePage> = {
  trainer: {
    path: "/trainer",
    label: "HLV Trưởng",
    description: "Huấn luyện & Thể lực",
    icon: Dumbbell,
    sections: [
      overview,
      { id: "fitness", label: "Tiến độ thể lực", icon: Activity },
      { id: "alerts", label: "Cảnh báo trực tiếp", icon: Bell },
      { id: "schedule", label: "Lịch huấn luyện", icon: CalendarDays },
    ],
  },
  vet: {
    path: "/vet",
    label: "Bác sĩ thú y",
    description: "Y tế & Chuồng trại",
    icon: Stethoscope,
    sections: [
      overview,
      { id: "stalls", label: "Sơ đồ chuồng trại", icon: Home },
      { id: "medical", label: "Hồ sơ y tế", icon: HeartPulse },
    ],
  },
  groom: {
    path: "/groom",
    label: "Nhân viên chuồng",
    description: "Chăm sóc hằng ngày",
    icon: ClipboardCheck,
    sections: [
      overview,
      { id: "tasks", label: "Công việc ca trực", icon: ClipboardCheck },
      { id: "nutrition", label: "Khẩu phần dinh dưỡng", icon: Utensils },
      { id: "incidents", label: "Báo cáo sự cố", icon: Bell },
    ],
  },
  owner: {
    path: "/owner",
    label: "Chủ ngựa",
    description: "Hồ sơ & Phả hệ",
    icon: Trophy,
    sections: [
      overview,
      { id: "profile", label: "Hồ sơ & Phả hệ", icon: Trophy },
      { id: "races", label: "Thành tích thi đấu", icon: BarChart3 },
      { id: "health", label: "Thể lực & Sức khỏe", icon: HeartPulse },
    ],
  },
  manager: {
    path: "/manager",
    label: "Quản lý CLB",
    description: "Tài chính & Điều hành",
    icon: BarChart3,
    sections: [
      overview,
      { id: "finance", label: "Báo cáo tài chính", icon: BarChart3 },
      { id: "costs", label: "Cơ cấu chi phí", icon: CircleDollarSign },
      { id: "operations", label: "Nhân sự, kho & Nhật ký", icon: Users },
    ],
  },
};

export function isRole(value: unknown): value is Role {
  return typeof value === "string" && Object.hasOwn(ROLE_PAGES, value);
}
