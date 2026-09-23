import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import { toast } from "sonner";
import { isRole, type Role } from "@/lib/roles";
export type { Role } from "@/lib/roles";

export interface User {
  id: string;
  name: string;
  email: string;
  role: Role;
  roleTitle: string;
  avatarUrl?: string;
  initials: string;
  phone?: string;
  stableName?: string;
}

export const DEMO_USERS: Record<Role, User> = {
  trainer: {
    id: "usr-trainer-01",
    name: "Nguyễn Văn A",
    email: "trainer@maphong.vn",
    role: "trainer",
    roleTitle: "HLV Trưởng",
    initials: "NA",
    phone: "0901 234 567",
    stableName: "Trại huấn luyện Trung tâm",
  },
  vet: {
    id: "usr-vet-01",
    name: "BS. Lê Thu Hà",
    email: "vet@maphong.vn",
    role: "vet",
    roleTitle: "Bác sĩ thú y",
    initials: "TH",
    phone: "0912 345 678",
    stableName: "Phòng khám Thú y Trường đua",
  },
  groom: {
    id: "usr-groom-01",
    name: "Trần Minh Khoa",
    email: "groom@maphong.vn",
    role: "groom",
    roleTitle: "Nhân viên chuồng",
    initials: "MK",
    phone: "0923 456 789",
    stableName: "Khu chuồng trại A–B",
  },
  owner: {
    id: "usr-owner-01",
    name: "Phạm Gia Huy",
    email: "owner@maphong.vn",
    role: "owner",
    roleTitle: "Chủ ngựa",
    initials: "GH",
    phone: "0934 567 890",
    stableName: "Chủ sở hữu Xích Thố & Bạch Long",
  },
  manager: {
    id: "usr-manager-01",
    name: "Hoàng Đức Nam",
    email: "manager@maphong.vn",
    role: "manager",
    roleTitle: "Quản lý CLB",
    initials: "ĐN",
    phone: "0945 678 901",
    stableName: "Ban điều hành CLB Mã Phong",
  },
};

const SESSION_KEY = "maphong_session_user";

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password?: string, requestedRole?: Role) => Promise<User | null>;
  register: (
    name: string,
    email: string,
    password: string,
    role: Role,
    phone?: string,
  ) => Promise<User | null>;
  logout: () => void;
  updateUser: (data: Partial<User>) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    try {
      // Clear legacy default trainer from localStorage if present
      localStorage.removeItem("maphong_current_user");

      const stored = sessionStorage.getItem(SESSION_KEY);
      if (stored) {
        const savedUser: User | null = JSON.parse(stored);
        setUser(savedUser && isRole(savedUser.role) ? savedUser : null);
      } else {
        // Default: unauthenticated on initial visit
        setUser(null);
      }
    } catch {
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const login = async (
    email: string,
    _password?: string,
    requestedRole?: Role,
  ): Promise<User | null> => {
    setIsLoading(true);
    try {
      // Find matching demo user by email or by requestedRole
      let matchedUser: User | undefined;
      if (requestedRole && DEMO_USERS[requestedRole]) {
        matchedUser = DEMO_USERS[requestedRole];
      } else {
        matchedUser = Object.values(DEMO_USERS).find(
          (u) => u.email.toLowerCase() === email.trim().toLowerCase(),
        );
      }

      if (!matchedUser) {
        // Generic user fallback
        const namePart = email.split("@")[0] || "Người dùng";
        const cleanName = namePart.charAt(0).toUpperCase() + namePart.slice(1);
        const fallbackRole = requestedRole || "trainer";
        matchedUser = {
          id: `usr-${Date.now()}`,
          name: cleanName,
          email: email.trim(),
          role: fallbackRole,
          roleTitle: DEMO_USERS[fallbackRole].roleTitle,
          initials: cleanName.slice(0, 2).toUpperCase(),
          phone: "0900 000 000",
        };
      }

      setUser(matchedUser);
      sessionStorage.setItem(SESSION_KEY, JSON.stringify(matchedUser));
      toast.success(`Chào mừng trở lại, ${matchedUser.name}!`, {
        description: `Đang ở chế độ: ${matchedUser.roleTitle}`,
      });
      return matchedUser;
    } catch (error) {
      toast.error("Đăng nhập thất bại", {
        description: error instanceof Error ? error.message : "Vui lòng kiểm tra lại thông tin.",
      });
      return null;
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (
    name: string,
    email: string,
    _password: string,
    role: Role,
    phone?: string,
  ): Promise<User | null> => {
    setIsLoading(true);
    try {
      const words = name.trim().split(/\s+/);
      const initials =
        words.length > 1
          ? `${words[0]?.[0] ?? ""}${words.at(-1)?.[0] ?? ""}`.toUpperCase()
          : name.slice(0, 2).toUpperCase();

      const newUser: User = {
        id: `usr-${Date.now()}`,
        name: name.trim(),
        email: email.trim(),
        role,
        roleTitle: DEMO_USERS[role].roleTitle,
        initials,
        phone: phone || "0900 000 000",
        stableName: "CLB Mã Phong",
      };

      setUser(newUser);
      sessionStorage.setItem(SESSION_KEY, JSON.stringify(newUser));
      toast.success("Đăng ký tài khoản thành công!", {
        description: `Chào mừng ${newUser.name} gia nhập hệ thống Mã Phong.`,
      });
      return newUser;
    } catch (error) {
      toast.error("Đăng ký thất bại", {
        description:
          error instanceof Error ? error.message : "Có lỗi xảy ra trong quá trình đăng ký.",
      });
      return null;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    setUser(null);
    sessionStorage.removeItem(SESSION_KEY);
    localStorage.removeItem("maphong_current_user");
    toast.info("Đã đăng xuất khỏi hệ thống.");
  };

  const updateUser = (data: Partial<User>) => {
    if (!user) return;
    const updated = { ...user, ...data };
    setUser(updated);
    sessionStorage.setItem(SESSION_KEY, JSON.stringify(updated));
    toast.success("Đã cập nhật thông tin cá nhân!");
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        register,
        logout,
        updateUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
