import { useState, type FormEvent } from "react";
import { useNavigate } from "@tanstack/react-router";
import {
  Trophy,
  Dumbbell,
  Stethoscope,
  ClipboardCheck,
  BarChart3,
  Mail,
  Lock,
  User as UserIcon,
  Phone,
  Sparkles,
  ArrowRight,
  ShieldCheck,
  X,
  Eye,
  EyeOff,
} from "lucide-react";
import { useAuth, DEMO_USERS, type Role } from "@/context/auth-context";

interface AuthModalProps {
  isOpen: boolean;
  onClose: () => void;
  defaultTab?: "login" | "register";
}

export function AuthModal({ isOpen, onClose, defaultTab = "login" }: AuthModalProps) {
  const { login, register, isLoading } = useAuth();
  const navigate = useNavigate();
  const [tab, setTab] = useState<"login" | "register">(defaultTab);
  const [showPassword, setShowPassword] = useState(false);

  // Login form state
  const [loginEmail, setLoginEmail] = useState("");
  const [loginPassword, setLoginPassword] = useState("");

  // Register form state
  const [regName, setRegName] = useState("");
  const [regEmail, setRegEmail] = useState("");
  const [regPhone, setRegPhone] = useState("");
  const [regPassword, setRegPassword] = useState("");
  const [regRole, setRegRole] = useState<Role>("trainer");

  if (!isOpen) return null;

  const handleLoginSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!loginEmail) return;
    const loggedUser = await login(loginEmail, loginPassword);
    if (loggedUser) {
      onClose();
      navigate({ to: `/${loggedUser.role}` as any });
    }
  };

  const handleRegisterSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!regName || !regEmail || !regPassword) return;
    const newUser = await register(regName, regEmail, regPassword, regRole, regPhone);
    if (newUser) {
      onClose();
      navigate({ to: `/${newUser.role}` as any });
    }
  };

  const handleQuickLogin = async (role: Role) => {
    const demo = DEMO_USERS[role];
    const loggedUser = await login(demo.email, "demo123", role);
    if (loggedUser) {
      onClose();
      navigate({ to: `/${role}` as any });
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-overlay/80 p-4 backdrop-blur-xs animate-fade-in" role="dialog" aria-modal="true">
      <div className="relative w-full max-w-lg overflow-hidden rounded-xl border border-border bg-card shadow-modal animate-scale-in">
        {/* Header with theme banner */}
        <div className="relative bg-gradient-to-r from-sidebar via-primary to-secondary p-6 text-sidebar-primary-foreground">
          <button
            onClick={onClose}
            aria-label="Đóng"
            className="absolute right-4 top-4 grid size-8 place-items-center rounded-full bg-black/20 text-white transition hover:bg-black/40"
          >
            <X size={18} />
          </button>
          <div className="flex items-center gap-3">
            <div className="grid size-12 place-items-center rounded-lg bg-white/10 text-white backdrop-blur-md">
              <Trophy size={26} />
            </div>
            <div>
              <h2 className="font-display text-xl font-bold tracking-tight">MÃ PHONG</h2>
              <p className="text-xs text-white/80">Hệ thống Quản lý Huấn luyện Ngựa đua</p>
            </div>
          </div>

          {/* Navigation Tabs */}
          <div className="mt-6 flex rounded-lg bg-black/20 p-1 backdrop-blur-sm">
            <button
              onClick={() => setTab("login")}
              className={`flex-1 rounded-md py-2 text-center text-xs font-bold transition ${
                tab === "login"
                  ? "bg-white text-primary shadow-sm"
                  : "text-white/80 hover:text-white"
              }`}
            >
              Đăng nhập
            </button>
            <button
              onClick={() => setTab("register")}
              className={`flex-1 rounded-md py-2 text-center text-xs font-bold transition ${
                tab === "register"
                  ? "bg-white text-primary shadow-sm"
                  : "text-white/80 hover:text-white"
              }`}
            >
              Đăng ký tài khoản
            </button>
          </div>
        </div>

        {/* Form Body */}
        <div className="p-6">
          {tab === "login" ? (
            <div className="space-y-5">
              <form onSubmit={handleLoginSubmit} className="space-y-4">
                <div>
                  <label className="mb-1.5 block text-xs font-semibold text-foreground">
                    Email tài khoản
                  </label>
                  <div className="relative">
                    <Mail className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                    <input
                      type="email"
                      required
                      value={loginEmail}
                      onChange={(e) => setLoginEmail(e.target.value)}
                      placeholder="trainer@maphong.vn"
                      className="h-10 w-full rounded-md border border-input bg-muted/40 pl-10 pr-3 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-ring"
                    />
                  </div>
                </div>

                <div>
                  <div className="mb-1.5 flex items-center justify-between">
                    <label className="text-xs font-semibold text-foreground">Mật khẩu</label>
                    <button
                      type="button"
                      onClick={() => alert("Mật khẩu tài khoản demo mặc định: 123456")}
                      className="text-[11px] text-primary hover:underline"
                    >
                      Quên mật khẩu?
                    </button>
                  </div>
                  <div className="relative">
                    <Lock className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                    <input
                      type={showPassword ? "text" : "password"}
                      required
                      value={loginPassword}
                      onChange={(e) => setLoginPassword(e.target.value)}
                      placeholder="••••••••"
                      className="h-10 w-full rounded-md border border-input bg-muted/40 pl-10 pr-10 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-ring"
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                    >
                      {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                    </button>
                  </div>
                </div>

                <button
                  type="submit"
                  disabled={isLoading}
                  className="btn-primary w-full justify-center py-2.5 shadow-sm"
                >
                  <ArrowRight size={17} /> Đăng nhập hệ thống
                </button>
              </form>

              {/* 1-Click Quick Demo Login */}
              <div className="relative my-4">
                <div className="absolute inset-0 flex items-center">
                  <div className="w-full border-t border-border" />
                </div>
                <div className="relative flex justify-center text-[10px] uppercase">
                  <span className="bg-card px-2 font-bold text-muted-foreground">
                    Hoặc đăng nhập nhanh bằng tài khoản mẫu
                  </span>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-2 sm:grid-cols-3">
                {[
                  { role: "trainer" as Role, label: "HLV Trưởng", icon: Dumbbell, desc: "Nguyễn Văn A" },
                  { role: "vet" as Role, label: "Bác sĩ thú y", icon: Stethoscope, desc: "BS. Lê Thu Hà" },
                  { role: "groom" as Role, label: "Nhân viên chuồng", icon: ClipboardCheck, desc: "Trần Minh Khoa" },
                  { role: "owner" as Role, label: "Chủ ngựa", icon: Trophy, desc: "Phạm Gia Huy" },
                  { role: "manager" as Role, label: "Quản lý CLB", icon: BarChart3, desc: "Hoàng Đức Nam" },
                ].map((item) => {
                  const Icon = item.icon;
                  return (
                    <button
                      key={item.role}
                      type="button"
                      onClick={() => handleQuickLogin(item.role)}
                      className="group flex flex-col items-start rounded-lg border border-border bg-muted/30 p-2.5 text-left transition hover:border-primary hover:bg-primary-soft"
                    >
                      <span className="mb-1 grid size-7 place-items-center rounded-md bg-card text-primary shadow-xs transition group-hover:bg-primary group-hover:text-primary-foreground">
                        <Icon size={15} />
                      </span>
                      <strong className="block text-xs font-semibold">{item.label}</strong>
                      <span className="block truncate text-[10px] text-muted-foreground">{item.desc}</span>
                    </button>
                  );
                })}
              </div>
            </div>
          ) : (
            <form onSubmit={handleRegisterSubmit} className="space-y-4">
              <div>
                <label className="mb-1.5 block text-xs font-semibold text-foreground">
                  Họ và tên
                </label>
                <div className="relative">
                  <UserIcon className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                  <input
                    type="text"
                    required
                    value={regName}
                    onChange={(e) => setRegName(e.target.value)}
                    placeholder="Ví dụ: Hoàng Tuấn Kiệt"
                    className="h-10 w-full rounded-md border border-input bg-muted/40 pl-10 pr-3 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-ring"
                  />
                </div>
              </div>

              <div className="grid gap-3 sm:grid-cols-2">
                <div>
                  <label className="mb-1.5 block text-xs font-semibold text-foreground">
                    Email
                  </label>
                  <div className="relative">
                    <Mail className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                    <input
                      type="email"
                      required
                      value={regEmail}
                      onChange={(e) => setRegEmail(e.target.value)}
                      placeholder="kiet@maphong.vn"
                      className="h-10 w-full rounded-md border border-input bg-muted/40 pl-10 pr-3 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-ring"
                    />
                  </div>
                </div>
                <div>
                  <label className="mb-1.5 block text-xs font-semibold text-foreground">
                    Số điện thoại
                  </label>
                  <div className="relative">
                    <Phone className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                    <input
                      type="tel"
                      value={regPhone}
                      onChange={(e) => setRegPhone(e.target.value)}
                      placeholder="0988 123 456"
                      className="h-10 w-full rounded-md border border-input bg-muted/40 pl-10 pr-3 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-ring"
                    />
                  </div>
                </div>
              </div>

              <div>
                <label className="mb-1.5 block text-xs font-semibold text-foreground">
                  Vai trò trong trường đua
                </label>
                <select
                  value={regRole}
                  onChange={(e) => setRegRole(e.target.value as Role)}
                  className="field h-10"
                >
                  <option value="trainer">Huấn luyện viên Trưởng</option>
                  <option value="vet">Bác sĩ Thú y</option>
                  <option value="groom">Nhân viên Chăm sóc / Chuồng</option>
                  <option value="owner">Chủ sở hữu Ngựa đua</option>
                  <option value="manager">Quản lý & Điều hành CLB</option>
                </select>
              </div>

              <div>
                <label className="mb-1.5 block text-xs font-semibold text-foreground">
                  Mật khẩu khởi tạo
                </label>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                  <input
                    type="password"
                    required
                    value={regPassword}
                    onChange={(e) => setRegPassword(e.target.value)}
                    placeholder="Ít nhất 6 ký tự..."
                    className="h-10 w-full rounded-md border border-input bg-muted/40 pl-10 pr-3 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-ring"
                  />
                </div>
              </div>

              <div className="flex items-center gap-2 rounded-md bg-muted/50 p-2.5 text-xs text-muted-foreground">
                <ShieldCheck size={16} className="text-success shrink-0" />
                Tài khoản mới được gán quyền tự động theo vai trò đã chọn.
              </div>

              <button
                type="submit"
                disabled={isLoading}
                className="btn-primary w-full justify-center py-2.5 shadow-sm"
              >
                <Sparkles size={17} /> Đăng ký & Bắt đầu làm việc
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}
