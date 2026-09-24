import { createFileRoute } from "@tanstack/react-router";
import { useState, type FormEvent } from "react";
import {
  BarChart3,
  CircleDollarSign,
  FileClock,
  FileSpreadsheet,
  FileText,
  Package,
  Plus,
  Trophy,
  Users,
  X,
} from "lucide-react";
import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { toast } from "sonner";
import { AppLayout } from "@/components/layout/app-layout";
import { useStable } from "@/context/stable-context";

export const Route = createFileRoute("/manager")({
  head: () => ({
    meta: [
      { title: "Quản lý CLB — Điều hành & Tài chính | Mã Phong" },
      {
        name: "description",
        content: "Báo cáo tài chính 6 tháng, phân quyền nhân sự, vật tư kho và nhật ký thao tác.",
      },
    ],
  }),
  component: ManagerPage,
});

const financeData = [
  { month: "T1", chiPhi: 340, doanhThu: 520 },
  { month: "T2", chiPhi: 390, doanhThu: 460 },
  { month: "T3", chiPhi: 360, doanhThu: 610 },
  { month: "T4", chiPhi: 430, doanhThu: 580 },
  { month: "T5", chiPhi: 410, doanhThu: 720 },
  { month: "T6", chiPhi: 470, doanhThu: 790 },
];

function ManagerPage() {
  const { auditLogs, addAuditLog } = useStable();
  const [tab, setTab] = useState<"staff" | "stock" | "log">("staff");
  const [exportModal, setExportModal] = useState(false);

  // Staff Table State
  const [staffList, setStaffList] = useState([
    {
      id: "1",
      name: "Nguyễn Văn A",
      title: "HLV Trưởng",
      shift: "05:30–14:00",
      access: "Toàn quyền huấn luyện",
    },
    {
      id: "2",
      name: "BS. Lê Thu Hà",
      title: "Bác sĩ thú y",
      shift: "07:00–17:00",
      access: "Y tế & khóa lịch",
    },
    {
      id: "3",
      name: "Trần Minh Khoa",
      title: "Nhân viên chuồng",
      shift: "05:00–13:00",
      access: "Chăm sóc & báo cáo",
    },
    {
      id: "4",
      name: "Phạm Ngọc Lan",
      title: "Kế toán",
      shift: "08:00–17:00",
      access: "Tài chính chỉ đọc",
    },
  ]);
  const [permModal, setPermModal] = useState(false);
  const [selectedStaff, setSelectedStaff] = useState<(typeof staffList)[number] | null>(null);
  const [newAccess, setNewAccess] = useState("");

  // Stock Table State
  const [stockList, setStockList] = useState([
    { id: "1", name: "Cỏ Timothy", qty: 680, unit: "kg", status: "Đủ 18 ngày", isLow: false },
    {
      id: "2",
      name: "Ngũ cốc hiệu suất cao",
      qty: 245,
      unit: "kg",
      status: "Đủ 12 ngày",
      isLow: false,
    },
    {
      id: "3",
      name: "Bộ băng bảo vệ móng & gân",
      qty: 14,
      unit: "bộ",
      status: "Cần nhập thêm",
      isLow: true,
    },
  ]);
  const [orderModal, setOrderModal] = useState(false);
  const [orderItem, setOrderItem] = useState<(typeof stockList)[number] | null>(null);
  const [orderAmount, setOrderAmount] = useState("100");

  const handleUpdatePermission = (e: FormEvent) => {
    e.preventDefault();
    if (!selectedStaff) return;
    setStaffList((prev) =>
      prev.map((s) => (s.id === selectedStaff.id ? { ...s, access: newAccess } : s)),
    );
    addAuditLog(`Quản lý thay đổi quyền truy cập của ${selectedStaff.name} thành "${newAccess}"`);
    toast.success(`Đã cập nhật phân quyền cho ${selectedStaff.name}!`);
    setPermModal(false);
  };

  const handleOrderStock = (e: FormEvent) => {
    e.preventDefault();
    if (!orderItem) return;
    const addQty = parseInt(orderAmount) || 0;
    setStockList((prev) =>
      prev.map((item) =>
        item.id === orderItem.id
          ? {
              ...item,
              qty: item.qty + addQty,
              status: "Đã bổ sung",
              isLow: false,
            }
          : item,
      ),
    );
    addAuditLog(`Đặt hàng bổ sung vật tư: ${orderAmount} ${orderItem.unit} ${orderItem.name}`);
    toast.success(`Đã tạo phiếu nhập ${orderAmount} ${orderItem.unit} ${orderItem.name}!`);
    setOrderModal(false);
  };

  const handleExportReport = (type: "excel" | "pdf") => {
    addAuditLog(`Xuất báo cáo tài chính & vận hành định dạng ${type.toUpperCase()}`);
    toast.success(`Đang tạo và tải xuống Báo cáo vận hành CLB (${type.toUpperCase()})...`);
    setExportModal(false);
  };

  return (
    <AppLayout role="manager">
      <div className="mb-6 grid gap-4 sm:grid-cols-[minmax(0,1fr)_auto] sm:items-end">
        <div className="min-w-0">
          <p className="mb-1 text-xs font-bold uppercase tracking-[0.14em] text-secondary">
            Phân hệ Điều hành
          </p>
          <h1 className="font-display text-2xl font-bold sm:text-3xl">
            Hiệu suất Vận hành & Tài chính
          </h1>
          <p className="mt-1 max-w-2xl text-sm text-muted-foreground">
            Tổng hợp tháng 9/2026 • Đơn vị tiền tệ: triệu đồng.
          </p>
        </div>
        <button onClick={() => setExportModal(true)} className="btn-primary hidden sm:inline-flex">
          <FileClock size={17} /> Xuất báo cáo CLB
        </button>
      </div>

      {/* Stat Grid */}
      <div className="mb-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <div className="rounded-lg border border-border bg-card p-5 shadow-card">
          <div className="flex items-start justify-between">
            <span className="stat-icon good">
              <CircleDollarSign size={19} />
            </span>
            <span className="text-[10px] font-bold uppercase text-muted-foreground">Hôm nay</span>
          </div>
          <div className="mt-5 text-2xl font-bold">790 tr</div>
          <div className="mt-1 text-xs font-semibold text-muted-foreground">Doanh thu tháng</div>
          <div className="mt-3 text-xs text-success">↑ 9,7% so với tháng trước</div>
        </div>

        <div className="rounded-lg border border-border bg-card p-5 shadow-card">
          <div className="flex items-start justify-between">
            <span className="stat-icon primary">
              <Package size={19} />
            </span>
            <span className="text-[10px] font-bold uppercase text-muted-foreground">Hôm nay</span>
          </div>
          <div className="mt-5 text-2xl font-bold">470 tr</div>
          <div className="mt-1 text-xs font-semibold text-muted-foreground">Chi phí vận hành</div>
          <div className="mt-3 text-xs text-success">Trong ngân sách kế hoạch</div>
        </div>

        <div className="rounded-lg border border-border bg-card p-5 shadow-card">
          <div className="flex items-start justify-between">
            <span className="stat-icon primary">
              <Users size={19} />
            </span>
            <span className="text-[10px] font-bold uppercase text-muted-foreground">Hôm nay</span>
          </div>
          <div className="mt-5 text-2xl font-bold">32</div>
          <div className="mt-1 text-xs font-semibold text-muted-foreground">Nhân sự hoạt động</div>
          <div className="mt-3 text-xs text-success">94% lịch trực đã phủ</div>
        </div>

        <div className="rounded-lg border border-border bg-card p-5 shadow-card">
          <div className="flex items-start justify-between">
            <span className="stat-icon warn">
              <Trophy size={19} />
            </span>
            <span className="text-[10px] font-bold uppercase text-muted-foreground">Hôm nay</span>
          </div>
          <div className="mt-5 text-2xl font-bold">1,84 tỷ</div>
          <div className="mt-1 text-xs font-semibold text-muted-foreground">
            Tiền thưởng mùa giải
          </div>
          <div className="mt-3 text-xs text-warning">4 chiến mã có thành tích</div>
        </div>
      </div>

      <div className="grid gap-6 xl:grid-cols-[1.2fr_0.8fr]">
        {/* Financial Chart */}
        <section
          id="finance"
          className="scroll-mt-24 rounded-lg border border-border bg-card shadow-card"
        >
          <div className="grid grid-cols-[minmax(0,1fr)_auto] items-start gap-4 border-b border-border px-5 py-4">
            <div className="flex min-w-0 items-start gap-3">
              <span className="mt-0.5 grid size-8 shrink-0 place-items-center rounded-md bg-primary-soft text-primary">
                <BarChart3 size={17} />
              </span>
              <div>
                <h2 className="font-display text-base font-bold">Tài chính 6 tháng gần nhất</h2>
                <p className="text-xs text-muted-foreground">
                  Chi phí vận hành và doanh thu giải đấu (triệu VNĐ)
                </p>
              </div>
            </div>
          </div>
          <div className="h-[315px] p-4">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={financeData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="var(--chart-grid)" />
                <XAxis dataKey="month" axisLine={false} tickLine={false} />
                <YAxis axisLine={false} tickLine={false} />
                <Tooltip />
                <Legend />
                <Bar dataKey="chiPhi" name="Chi phí" fill="var(--chart-2)" radius={[4, 4, 0, 0]} />
                <Bar
                  dataKey="doanhThu"
                  name="Doanh thu"
                  fill="var(--chart-1)"
                  radius={[4, 4, 0, 0]}
                />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </section>

        {/* Cost Breakdown */}
        <section
          id="costs"
          className="scroll-mt-24 rounded-lg border border-border bg-card shadow-card"
        >
          <div className="grid grid-cols-[minmax(0,1fr)_auto] items-start gap-4 border-b border-border px-5 py-4">
            <div className="flex min-w-0 items-start gap-3">
              <span className="mt-0.5 grid size-8 shrink-0 place-items-center rounded-md bg-primary-soft text-primary">
                <CircleDollarSign size={17} />
              </span>
              <div>
                <h2 className="font-display text-base font-bold">Cơ cấu chi phí tháng 9</h2>
                <p className="text-xs text-muted-foreground">Tổng 470 triệu đồng</p>
              </div>
            </div>
          </div>
          <div className="h-[315px] p-5">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart
                data={[
                  { n: "Thức ăn", v: 148 },
                  { n: "Y tế", v: 96 },
                  { n: "Nhân sự", v: 132 },
                  { n: "Bảo trì", v: 58 },
                  { n: "Khác", v: 36 },
                ]}
                layout="vertical"
              >
                <XAxis type="number" hide />
                <YAxis
                  dataKey="n"
                  type="category"
                  axisLine={false}
                  tickLine={false}
                  width={65}
                  tick={{ fontSize: 11 }}
                />
                <Tooltip />
                <Area
                  type="monotone"
                  dataKey="v"
                  fill="var(--primary-soft)"
                  stroke="var(--primary)"
                  strokeWidth={2}
                />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </section>

        {/* Sub-tabs */}
        <section
          id="operations"
          className="scroll-mt-24 rounded-lg border border-border bg-card shadow-card xl:col-span-2"
        >
          <div className="border-b border-border px-5 pt-4">
            <div className="flex gap-5 overflow-x-auto">
              {(
                [
                  ["staff", "Nhân sự & Phân quyền", Users],
                  ["stock", "Quản lý vật tư kho", Package],
                  ["log", "Nhật ký thao tác hệ thống", FileClock],
                ] as const
              ).map(([id, label, Icon]) => (
                <button
                  key={id}
                  onClick={() => setTab(id)}
                  className={`tab ${tab === id ? "active font-bold" : ""}`}
                >
                  <Icon size={16} />
                  {label}
                </button>
              ))}
            </div>
          </div>

          {tab === "staff" && (
            <div className="overflow-x-auto">
              <table>
                <thead>
                  <tr>
                    <th>Nhân sự</th>
                    <th>Vị trí</th>
                    <th>Ca trực</th>
                    <th>Quyền truy cập</th>
                    <th>Hành động</th>
                  </tr>
                </thead>
                <tbody>
                  {staffList.map((r) => (
                    <tr key={r.id}>
                      <td>
                        <div className="flex items-center gap-3">
                          <span className="grid size-8 place-items-center rounded-full bg-primary-soft text-xs font-bold text-primary">
                            {r.name.trim().slice(-1) || "N"}
                          </span>
                          <strong>{r.name}</strong>
                        </div>
                      </td>
                      <td>{r.title}</td>
                      <td>{r.shift}</td>
                      <td>
                        <span className="status neutral">{r.access}</span>
                      </td>
                      <td>
                        <button
                          onClick={() => {
                            setSelectedStaff(r);
                            setNewAccess(r.access);
                            setPermModal(true);
                          }}
                          className="btn-secondary text-xs py-1 px-2.5"
                        >
                          Phân quyền
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {tab === "stock" && (
            <div className="p-5 space-y-4">
              <div className="flex justify-between items-center pb-2">
                <p className="text-xs text-muted-foreground">
                  Tình trạng vật tư thức ăn & y tế chuồng trại
                </p>
                <button
                  onClick={() => {
                    setOrderItem(stockList[0] ?? null);
                    setOrderModal(true);
                  }}
                  className="btn-primary text-xs py-1.5 px-3"
                >
                  <Plus size={15} /> Nhập thêm vật tư
                </button>
              </div>

              <div className="grid gap-4 md:grid-cols-3">
                {stockList.map((item) => (
                  <div key={item.id} className="rounded-md border border-border p-4 bg-muted/20">
                    <div className="flex justify-between items-start">
                      <Package className="mb-3 text-primary" size={22} />
                      <button
                        onClick={() => {
                          setOrderItem(item);
                          setOrderModal(true);
                        }}
                        className="text-[11px] text-primary hover:underline font-bold"
                      >
                        + Bổ sung
                      </button>
                    </div>
                    <strong className="block text-sm">{item.name}</strong>
                    <span className="mt-1 block text-2xl font-bold">
                      {item.qty} {item.unit}
                    </span>
                    <small
                      className={item.isLow ? "text-danger font-semibold" : "text-muted-foreground"}
                    >
                      {item.status}
                    </small>
                  </div>
                ))}
              </div>
            </div>
          )}

          {tab === "log" && (
            <div className="divide-y divide-border">
              {auditLogs.map((r) => (
                <div
                  key={r.id}
                  className="grid grid-cols-[52px_1fr] gap-3 px-5 py-4 sm:grid-cols-[70px_170px_1fr] hover:bg-muted/30 transition"
                >
                  <span className="text-xs font-bold text-primary">{r.time}</span>
                  <strong className="text-sm">{r.actor}</strong>
                  <span className="text-xs text-muted-foreground">{r.action}</span>
                </div>
              ))}
            </div>
          )}
        </section>
      </div>

      {/* Permission Modal */}
      {permModal && selectedStaff && (
        <div
          className="fixed inset-0 z-50 grid place-items-center bg-overlay/80 p-4 backdrop-blur-xs animate-fade-in"
          role="dialog"
        >
          <div className="w-full max-w-lg overflow-hidden rounded-lg border border-border bg-card shadow-modal animate-scale-in">
            <div className="flex items-center justify-between border-b border-border px-5 py-4">
              <h2 className="font-display text-lg font-bold">
                Phân quyền truy cập: {selectedStaff.name}
              </h2>
              <button
                className="grid size-8 place-items-center rounded-md hover:bg-muted"
                onClick={() => setPermModal(false)}
              >
                <X size={18} />
              </button>
            </div>
            <form onSubmit={handleUpdatePermission} className="space-y-4 p-5">
              <p className="text-xs font-semibold text-muted-foreground">
                Vị trí hiện tại: {selectedStaff.title}
              </p>
              <label className="block">
                <span className="mb-1.5 block text-xs font-semibold">Quyền hạn truy cập</span>
                <select
                  value={newAccess}
                  onChange={(e) => setNewAccess(e.target.value)}
                  className="field"
                >
                  <option value="Toàn quyền huấn luyện">Toàn quyền huấn luyện</option>
                  <option value="Y tế & khóa lịch">Y tế & khóa lịch khẩn cấp</option>
                  <option value="Chăm sóc & báo cáo">Chăm sóc & báo cáo sự cố</option>
                  <option value="Tài chính chỉ đọc">Tài chính chỉ đọc</option>
                  <option value="Toàn quyền quản trị CLB">Toàn quyền quản trị CLB</option>
                </select>
              </label>
              <div className="flex justify-end gap-3 pt-2">
                <button type="button" className="btn-secondary" onClick={() => setPermModal(false)}>
                  Hủy
                </button>
                <button type="submit" className="btn-primary">
                  Lưu phân quyền
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Stock Order Modal */}
      {orderModal && orderItem && (
        <div
          className="fixed inset-0 z-50 grid place-items-center bg-overlay/80 p-4 backdrop-blur-xs animate-fade-in"
          role="dialog"
        >
          <div className="w-full max-w-lg overflow-hidden rounded-lg border border-border bg-card shadow-modal animate-scale-in">
            <div className="flex items-center justify-between border-b border-border px-5 py-4">
              <h2 className="font-display text-lg font-bold">Đặt hàng bổ sung: {orderItem.name}</h2>
              <button
                className="grid size-8 place-items-center rounded-md hover:bg-muted"
                onClick={() => setOrderModal(false)}
              >
                <X size={18} />
              </button>
            </div>
            <form onSubmit={handleOrderStock} className="space-y-4 p-5">
              <label className="block">
                <span className="mb-1.5 block text-xs font-semibold">Loại vật tư</span>
                <select
                  value={orderItem.id}
                  onChange={(e) => {
                    const it = stockList.find((x) => x.id === e.target.value);
                    if (it) setOrderItem(it);
                  }}
                  className="field"
                >
                  {stockList.map((x) => (
                    <option key={x.id} value={x.id}>
                      {x.name} (Hiện có: {x.qty} {x.unit})
                    </option>
                  ))}
                </select>
              </label>
              <label className="block">
                <span className="mb-1.5 block text-xs font-semibold">
                  Số lượng cần nhập ({orderItem.unit})
                </span>
                <input
                  type="number"
                  min="1"
                  required
                  value={orderAmount}
                  onChange={(e) => setOrderAmount(e.target.value)}
                  className="field"
                />
              </label>
              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  className="btn-secondary"
                  onClick={() => setOrderModal(false)}
                >
                  Hủy
                </button>
                <button type="submit" className="btn-primary">
                  Xác nhận tạo phiếu nhập
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Export Report Modal */}
      {exportModal && (
        <div
          className="fixed inset-0 z-50 grid place-items-center bg-overlay/80 p-4 backdrop-blur-xs animate-fade-in"
          role="dialog"
        >
          <div className="w-full max-w-lg overflow-hidden rounded-lg border border-border bg-card shadow-modal animate-scale-in">
            <div className="flex items-center justify-between border-b border-border px-5 py-4">
              <h2 className="font-display text-lg font-bold">Xuất báo cáo tổng hợp CLB</h2>
              <button
                className="grid size-8 place-items-center rounded-md hover:bg-muted"
                onClick={() => setExportModal(false)}
              >
                <X size={18} />
              </button>
            </div>
            <div className="p-5 space-y-4">
              <p className="text-xs text-muted-foreground">
                Báo cáo bao gồm: Doanh thu giải đấu, Chi phí vận hành tháng 9, Tiến độ thể lực đàn
                ngựa, Nhật ký phân quyền nhân sự.
              </p>
              <div className="grid grid-cols-2 gap-3">
                <button
                  onClick={() => handleExportReport("excel")}
                  className="flex flex-col items-center justify-center p-4 rounded-lg border border-border bg-muted/30 hover:border-primary hover:bg-primary-soft transition"
                >
                  <FileSpreadsheet className="text-success mb-2" size={28} />
                  <span className="text-xs font-bold">Xuất file Excel (.xlsx)</span>
                </button>
                <button
                  onClick={() => handleExportReport("pdf")}
                  className="flex flex-col items-center justify-center p-4 rounded-lg border border-border bg-muted/30 hover:border-primary hover:bg-primary-soft transition"
                >
                  <FileText className="text-danger mb-2" size={28} />
                  <span className="text-xs font-bold">Xuất file PDF (.pdf)</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </AppLayout>
  );
}
