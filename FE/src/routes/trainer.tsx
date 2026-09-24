import { createFileRoute } from "@tanstack/react-router";
import { useState, type FormEvent, type ReactNode } from "react";
import {
  Activity,
  AlertTriangle,
  Bell,
  CalendarDays,
  Check,
  Dumbbell,
  HeartPulse,
  Plus,
  Sparkles,
  Trophy,
  X,
} from "lucide-react";
import {
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { toast } from "sonner";
import bannerImage from "@/assets/racehorse-banner.jpg";
import { AppLayout } from "@/components/layout/app-layout";
import { useStable, type ScheduleItem } from "@/context/stable-context";

export const Route = createFileRoute("/trainer")({
  head: () => ({
    meta: [
      { title: "HLV Trưởng — Quản lý Huấn luyện | Mã Phong" },
      { name: "description", content: "Trung tâm điều phối và giáo án huấn luyện chiến mã." },
    ],
  }),
  component: TrainerPage,
});

const trainingData7Days = [
  { day: "T2", xichTho: 72, caviar: 66, saoMai: 61 },
  { day: "T3", xichTho: 76, caviar: 70, saoMai: 65 },
  { day: "T4", xichTho: 74, caviar: 75, saoMai: 69 },
  { day: "T5", xichTho: 82, caviar: 78, saoMai: 72 },
  { day: "T6", xichTho: 86, caviar: 81, saoMai: 76 },
  { day: "T7", xichTho: 89, caviar: 85, saoMai: 80 },
  { day: "CN", xichTho: 92, caviar: 87, saoMai: 84 },
];

const trainingData30Days = [
  { day: "Tuần 1", xichTho: 68, caviar: 64, saoMai: 58 },
  { day: "Tuần 2", xichTho: 75, caviar: 71, saoMai: 66 },
  { day: "Tuần 3", xichTho: 83, caviar: 79, saoMai: 74 },
  { day: "Tuần 4", xichTho: 92, caviar: 87, saoMai: 84 },
];

function TrainerPage() {
  const { horses, schedules, setSchedules, addAuditLog, addNotification } = useStable();
  const [modal, setModal] = useState(false);
  const [scheduleModal, setScheduleModal] = useState(false);
  const [newScheduleModal, setNewScheduleModal] = useState(false);
  const [saved, setSaved] = useState(false);
  const [chartTimeframe, setChartTimeframe] = useState<"7days" | "30days">("7days");

  // Form states
  const [selectedHorse, setSelectedHorse] = useState("Xích Thố");
  const [score, setScore] = useState("9");
  const [comment, setComment] = useState("Khả năng bứt tốc tốt, nhịp thở ổn định sau vòng cuối.");

  // New exercise form
  const [newTime, setNewTime] = useState("10:00");
  const [newHorse, setNewHorse] = useState("Thiên Mã");
  const [newExercise, setNewExercise] = useState("Chạy dằn sức 1.800m");
  const [newTrack, setNewTrack] = useState("Sân cát");

  const handleEvaluationSubmit = (e: FormEvent) => {
    e.preventDefault();
    setSaved(true);
    addAuditLog(`Đánh giá phong độ ${selectedHorse}: ${score}/10 điểm - "${comment}"`);
    addNotification(
      "Đánh giá phong độ mới",
      `HLV Trưởng vừa chấm ${score}/10 điểm cho ${selectedHorse}.`,
      "info",
    );
    toast.success(`Đã lưu đánh giá phong độ cho ${selectedHorse} (${score}/10 điểm)!`);
    setTimeout(() => {
      setSaved(false);
      setModal(false);
    }, 600);
  };

  const handleAddSchedule = (e: FormEvent) => {
    e.preventDefault();
    const newItem: ScheduleItem = {
      id: `sch-${Date.now()}`,
      time: newTime,
      horse: newHorse,
      exercise: newExercise,
      track: newTrack,
      status: "Sắp tới",
    };
    setSchedules((prev) => [...prev, newItem]);
    addAuditLog(`Thêm lịch huấn luyện mới cho ${newHorse} lúc ${newTime}`);
    toast.success(`Đã thêm buổi tập mới cho ${newHorse} vào lịch!`);
    setNewScheduleModal(false);
  };

  return (
    <AppLayout role="trainer">
      <div className="mb-6 grid gap-4 sm:grid-cols-[minmax(0,1fr)_auto] sm:items-end">
        <div className="min-w-0">
          <p className="mb-1 text-xs font-bold uppercase tracking-[0.14em] text-secondary">
            Phân hệ Huấn luyện
          </p>
          <h1 className="font-display text-2xl font-bold sm:text-3xl">
            Trung tâm Huấn luyện & Phong độ
          </h1>
          <p className="mt-1 max-w-2xl text-sm text-muted-foreground">
            Tổng quan đội đua và giáo án huấn luyện chiến mã ngày 22 tháng 9, 2026.
          </p>
        </div>
        <button className="btn-primary hidden sm:inline-flex" onClick={() => setModal(true)}>
          <Sparkles size={17} /> Đánh giá phong độ
        </button>
      </div>

      {/* Hero Banner */}
      <div className="relative mb-6 min-h-[210px] overflow-hidden rounded-lg bg-hero text-hero-foreground shadow-elevated">
        <img
          src={bannerImage}
          width={1920}
          height={900}
          alt="Chiến mã đang thi đấu trên trường đua"
          className="absolute inset-0 h-full w-full object-cover"
        />
        <div className="absolute inset-0 bg-hero-overlay" />
        <div className="relative flex min-h-[210px] max-w-xl flex-col justify-end p-6 sm:p-8">
          <span className="mb-3 w-fit rounded-sm bg-hero-badge px-2.5 py-1 text-[10px] font-bold uppercase tracking-[0.16em]">
            Giải mùa thu • Còn 12 ngày
          </span>
          <h2 className="font-display text-2xl font-bold sm:text-3xl">
            Xích Thố đang ở phong độ đỉnh cao
          </h2>
          <p className="mt-2 text-sm text-hero-muted">
            Chỉ số sẵn sàng đạt 92%. Duy trì giáo án tăng tốc 1.600m trong tuần này.
          </p>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="mb-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <div className="rounded-lg border border-border bg-card p-5 shadow-card">
          <div className="flex items-start justify-between">
            <span className="stat-icon good">
              <Activity size={19} />
            </span>
            <span className="text-[10px] font-bold uppercase text-muted-foreground">Hôm nay</span>
          </div>
          <div className="mt-5 text-2xl font-bold">87%</div>
          <div className="mt-1 text-xs font-semibold text-muted-foreground">Thể lực trung bình</div>
          <div className="mt-3 text-xs text-success">↑ 4,2% so với tuần trước</div>
        </div>

        <div className="rounded-lg border border-border bg-card p-5 shadow-card">
          <div className="flex items-start justify-between">
            <span className="stat-icon primary">
              <Dumbbell size={19} />
            </span>
            <span className="text-[10px] font-bold uppercase text-muted-foreground">Hôm nay</span>
          </div>
          <div className="mt-5 text-2xl font-bold">0{schedules.length}</div>
          <div className="mt-1 text-xs font-semibold text-muted-foreground">Buổi tập hôm nay</div>
          <div className="mt-3 text-xs text-success">
            {schedules.filter((s) => s.status === "Hoàn thành").length} buổi đã hoàn thành
          </div>
        </div>

        <div className="rounded-lg border border-border bg-card p-5 shadow-card">
          <div className="flex items-start justify-between">
            <span className="stat-icon warn">
              <HeartPulse size={19} />
            </span>
            <span className="text-[10px] font-bold uppercase text-muted-foreground">Hôm nay</span>
          </div>
          <div className="mt-5 text-2xl font-bold">142 bpm</div>
          <div className="mt-1 text-xs font-semibold text-muted-foreground">
            Nhịp tim trung bình
          </div>
          <div className="mt-3 text-xs text-warning">Trong vùng mục tiêu an toàn</div>
        </div>

        <div className="rounded-lg border border-border bg-card p-5 shadow-card">
          <div className="flex items-start justify-between">
            <span className="stat-icon primary">
              <Trophy size={19} />
            </span>
            <span className="text-[10px] font-bold uppercase text-muted-foreground">Hôm nay</span>
          </div>
          <div className="mt-5 text-2xl font-bold">
            {horses.filter((h) => h.status === "Khỏe").length}/{horses.length}
          </div>
          <div className="mt-1 text-xs font-semibold text-muted-foreground">Sẵn sàng thi đấu</div>
          <div className="mt-3 text-xs text-warning">
            {horses.filter((h) => h.status !== "Khỏe").length} chiến mã cần theo dõi
          </div>
        </div>
      </div>

      <div className="grid gap-6 xl:grid-cols-[minmax(0,1.55fr)_minmax(320px,0.75fr)]">
        {/* Fitness Trend Chart */}
        <section
          id="fitness"
          className="scroll-mt-24 rounded-lg border border-border bg-card shadow-card"
        >
          <div className="grid grid-cols-[minmax(0,1fr)_auto] items-start gap-4 border-b border-border px-5 py-4">
            <div className="flex min-w-0 items-start gap-3">
              <span className="mt-0.5 grid size-8 shrink-0 place-items-center rounded-md bg-primary-soft text-primary">
                <Activity size={17} />
              </span>
              <div>
                <h2 className="font-display text-base font-bold">Tiến độ thể lực đội đua</h2>
                <p className="text-xs text-muted-foreground">
                  {chartTimeframe === "7days"
                    ? "Điểm thể lực 7 ngày gần nhất"
                    : "Điểm thể lực tổng hợp 30 ngày (theo tuần)"}
                </p>
              </div>
            </div>
            <select
              value={chartTimeframe}
              onChange={(e) => setChartTimeframe(e.target.value as "7days" | "30days")}
              className="field h-8 py-0 text-xs"
            >
              <option value="7days">7 ngày gần nhất</option>
              <option value="30days">30 ngày (theo tuần)</option>
            </select>
          </div>
          <div className="h-[310px] p-4">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={chartTimeframe === "7days" ? trainingData7Days : trainingData30Days}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="var(--chart-grid)" />
                <XAxis
                  dataKey="day"
                  axisLine={false}
                  tickLine={false}
                  tick={{ fill: "var(--muted-foreground)", fontSize: 11 }}
                />
                <YAxis
                  domain={[50, 100]}
                  axisLine={false}
                  tickLine={false}
                  tick={{ fill: "var(--muted-foreground)", fontSize: 11 }}
                />
                <Tooltip
                  contentStyle={{
                    borderRadius: 6,
                    border: "1px solid var(--border)",
                    boxShadow: "var(--shadow-card)",
                  }}
                />
                <Legend iconType="circle" />
                <Line
                  type="monotone"
                  dataKey="xichTho"
                  name="Xích Thố"
                  stroke="var(--chart-1)"
                  strokeWidth={3}
                  dot={{ r: 4 }}
                />
                <Line
                  type="monotone"
                  dataKey="caviar"
                  name="Black Caviar"
                  stroke="var(--chart-2)"
                  strokeWidth={2}
                  dot={{ r: 3 }}
                />
                <Line
                  type="monotone"
                  dataKey="saoMai"
                  name="Sao Mai"
                  stroke="var(--chart-3)"
                  strokeWidth={2}
                  dot={{ r: 3 }}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </section>

        {/* Live Alerts */}
        <section
          id="alerts"
          className="scroll-mt-24 rounded-lg border border-border bg-card shadow-card overflow-hidden"
        >
          <div className="grid grid-cols-[minmax(0,1fr)_auto] items-start gap-4 border-b border-border px-5 py-4">
            <div className="flex min-w-0 items-start gap-3">
              <span className="mt-0.5 grid size-8 shrink-0 place-items-center rounded-md bg-primary-soft text-primary">
                <Bell size={17} />
              </span>
              <div>
                <h2 className="font-display text-base font-bold">Cảnh báo trực tiếp</h2>
                <p className="text-xs text-muted-foreground">
                  Cập nhật từ thiết bị đeo GPS & nhịp tim
                </p>
              </div>
            </div>
          </div>
          <div className="space-y-3 p-4">
            <div
              onClick={() => toast.warning("Đã ghi nhận cảnh báo nhịp tim của Hắc Phong.")}
              className="rounded-md border p-3 cursor-pointer alert-severe transition hover:opacity-90"
            >
              <div className="flex gap-3">
                <AlertTriangle className="text-danger" size={18} />
                <div className="flex-1 min-w-0">
                  <div className="flex justify-between">
                    <p className="text-sm font-bold">Hắc Phong</p>
                    <span className="text-[10px] text-muted-foreground">2 phút trước</span>
                  </div>
                  <p className="mt-1 text-xs leading-relaxed">
                    Nhịp tim 196 bpm vượt ngưỡng an toàn (180 bpm)
                  </p>
                </div>
              </div>
            </div>

            <div
              onClick={() => toast.info("Đã yêu cầu HLV giảm cự ly cho Black Caviar.")}
              className="rounded-md border p-3 cursor-pointer bg-warning-soft border-warning-border transition hover:opacity-90"
            >
              <div className="flex gap-3">
                <AlertTriangle className="text-warning" size={18} />
                <div className="flex-1 min-w-0">
                  <div className="flex justify-between">
                    <p className="text-sm font-bold">Black Caviar</p>
                    <span className="text-[10px] text-muted-foreground">8 phút trước</span>
                  </div>
                  <p className="mt-1 text-xs leading-relaxed">
                    Vận tốc giảm 12% ở vòng 3 trên sân cát
                  </p>
                </div>
              </div>
            </div>

            <div
              onClick={() => toast.info("Đã chuyển thông báo thân nhiệt cho Bác sĩ thú y.")}
              className="rounded-md border p-3 cursor-pointer bg-warning-soft border-warning-border transition hover:opacity-90"
            >
              <div className="flex gap-3">
                <AlertTriangle className="text-warning" size={18} />
                <div className="flex-1 min-w-0">
                  <div className="flex justify-between">
                    <p className="text-sm font-bold">Thiên Mã</p>
                    <span className="text-[10px] text-muted-foreground">15 phút trước</span>
                  </div>
                  <p className="mt-1 text-xs leading-relaxed">
                    Nhiệt độ cơ thể 39,1°C sau buổi tập bơi
                  </p>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Schedule */}
        <section
          id="schedule"
          className="scroll-mt-24 rounded-lg border border-border bg-card shadow-card xl:col-span-2"
        >
          <div className="grid grid-cols-[minmax(0,1fr)_auto] items-start gap-4 border-b border-border px-5 py-4">
            <div className="flex min-w-0 items-start gap-3">
              <span className="mt-0.5 grid size-8 shrink-0 place-items-center rounded-md bg-primary-soft text-primary">
                <CalendarDays size={17} />
              </span>
              <div>
                <h2 className="font-display text-base font-bold">Lịch huấn luyện hôm nay</h2>
                <p className="text-xs text-muted-foreground">
                  Sân chính • Thời tiết 27°C, độ ẩm 68%
                </p>
              </div>
            </div>
            <div className="flex gap-2">
              <button
                onClick={() => setNewScheduleModal(true)}
                className="btn-primary text-xs py-1 px-2.5"
              >
                <Plus size={14} /> Thêm bài tập
              </button>
              <button onClick={() => setScheduleModal(true)} className="btn-ghost text-xs">
                Xem toàn bộ
              </button>
            </div>
          </div>
          <div className="divide-y divide-border">
            {schedules.map((row) => (
              <div
                key={row.id}
                className="grid grid-cols-[58px_minmax(0,1fr)] gap-3 px-5 py-4 sm:grid-cols-[70px_1fr_1fr_110px_auto] sm:items-center hover:bg-muted/40 transition"
              >
                <span className="text-sm font-bold text-primary">{row.time}</span>
                <div>
                  <p className="text-sm font-semibold">{row.horse}</p>
                  <p className="text-xs text-muted-foreground sm:hidden">
                    {row.exercise} • {row.track}
                  </p>
                </div>
                <span className="hidden text-sm sm:block">{row.exercise}</span>
                <span className="hidden text-xs text-muted-foreground sm:block">{row.track}</span>
                <span
                  className={`status ${row.status === "Hoàn thành" ? "done" : row.status === "Đang tập" ? "active" : "neutral"}`}
                >
                  {row.status}
                </span>
              </div>
            ))}
          </div>
        </section>
      </div>

      <button className="btn-primary mt-6 w-full sm:hidden" onClick={() => setModal(true)}>
        <Sparkles size={17} /> Đánh giá phong độ
      </button>

      {/* Evaluation Modal */}
      {modal && (
        <div
          className="fixed inset-0 z-50 grid place-items-center bg-overlay/80 p-4 backdrop-blur-xs animate-fade-in"
          role="dialog"
        >
          <div className="w-full max-w-lg overflow-hidden rounded-lg border border-border bg-card shadow-modal animate-scale-in">
            <div className="flex items-center justify-between border-b border-border px-5 py-4">
              <h2 className="font-display text-lg font-bold">Đánh giá phong độ sau buổi tập</h2>
              <button
                className="grid size-8 place-items-center rounded-md hover:bg-muted"
                onClick={() => setModal(false)}
              >
                <X size={18} />
              </button>
            </div>
            <form onSubmit={handleEvaluationSubmit} className="space-y-4 p-5">
              <label className="block">
                <span className="mb-1.5 block text-xs font-semibold">Chiến mã đánh giá</span>
                <select
                  value={selectedHorse}
                  onChange={(e) => setSelectedHorse(e.target.value)}
                  className="field"
                >
                  {horses.map((h) => (
                    <option key={h.stall} value={h.name}>
                      {h.name} ({h.stall} - {h.status})
                    </option>
                  ))}
                </select>
              </label>

              <label className="block">
                <span className="mb-1.5 block text-xs font-semibold">Điểm phong độ (1–10)</span>
                <div className="flex items-center gap-3">
                  <input
                    className="field"
                    type="number"
                    min="1"
                    max="10"
                    value={score}
                    onChange={(e) => setScore(e.target.value)}
                  />
                  <span className="text-sm font-bold text-primary">{score}/10</span>
                </div>
              </label>

              <label className="block">
                <span className="mb-1.5 block text-xs font-semibold">
                  Nhận xét của huấn luyện viên
                </span>
                <textarea
                  className="field min-h-24"
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  placeholder="Nhận xét về tốc độ bứt phá, nhịp thở, độ sẵn sàng..."
                />
              </label>

              <div className="flex justify-end gap-3 pt-2">
                <button type="button" className="btn-secondary" onClick={() => setModal(false)}>
                  Hủy
                </button>
                <button type="submit" className="btn-primary">
                  {saved ? (
                    <>
                      <Check size={17} /> Đã lưu
                    </>
                  ) : (
                    "Lưu đánh giá"
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Full schedule modal */}
      {scheduleModal && (
        <div
          className="fixed inset-0 z-50 grid place-items-center bg-overlay/80 p-4 backdrop-blur-xs animate-fade-in"
          role="dialog"
        >
          <div className="w-full max-w-lg overflow-hidden rounded-lg border border-border bg-card shadow-modal animate-scale-in">
            <div className="flex items-center justify-between border-b border-border px-5 py-4">
              <h2 className="font-display text-lg font-bold">Toàn bộ lịch huấn luyện trong tuần</h2>
              <button
                className="grid size-8 place-items-center rounded-md hover:bg-muted"
                onClick={() => setScheduleModal(false)}
              >
                <X size={18} />
              </button>
            </div>
            <div className="p-5 space-y-3">
              <div className="flex justify-between items-center pb-2 border-b border-border">
                <p className="text-xs text-muted-foreground">
                  Tổng cộng {schedules.length} buổi tập đã lên kế hoạch
                </p>
                <button
                  onClick={() => {
                    setScheduleModal(false);
                    setNewScheduleModal(true);
                  }}
                  className="btn-primary text-xs py-1 px-2"
                >
                  <Plus size={14} /> Thêm mới
                </button>
              </div>
              <div className="max-h-80 overflow-y-auto divide-y divide-border">
                {schedules.map((s) => (
                  <div key={s.id} className="py-2.5 flex items-center justify-between text-xs">
                    <div>
                      <span className="font-bold text-primary mr-2">{s.time}</span>
                      <span className="font-semibold">{s.horse}</span>
                      <p className="text-muted-foreground">
                        {s.exercise} • {s.track}
                      </p>
                    </div>
                    <span
                      className={`status ${s.status === "Hoàn thành" ? "done" : s.status === "Đang tập" ? "active" : "neutral"}`}
                    >
                      {s.status}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Add new schedule item modal */}
      {newScheduleModal && (
        <div
          className="fixed inset-0 z-50 grid place-items-center bg-overlay/80 p-4 backdrop-blur-xs animate-fade-in"
          role="dialog"
        >
          <div className="w-full max-w-lg overflow-hidden rounded-lg border border-border bg-card shadow-modal animate-scale-in">
            <div className="flex items-center justify-between border-b border-border px-5 py-4">
              <h2 className="font-display text-lg font-bold">Thêm buổi tập huấn luyện mới</h2>
              <button
                className="grid size-8 place-items-center rounded-md hover:bg-muted"
                onClick={() => setNewScheduleModal(false)}
              >
                <X size={18} />
              </button>
            </div>
            <form onSubmit={handleAddSchedule} className="space-y-4 p-5">
              <div className="grid grid-cols-2 gap-3">
                <label className="block">
                  <span className="mb-1.5 block text-xs font-semibold">Giờ tập</span>
                  <input
                    type="time"
                    required
                    value={newTime}
                    onChange={(e) => setNewTime(e.target.value)}
                    className="field"
                  />
                </label>
                <label className="block">
                  <span className="mb-1.5 block text-xs font-semibold">Chiến mã</span>
                  <select
                    value={newHorse}
                    onChange={(e) => setNewHorse(e.target.value)}
                    className="field"
                  >
                    {horses.map((h) => (
                      <option key={h.stall} value={h.name}>
                        {h.name}
                      </option>
                    ))}
                  </select>
                </label>
              </div>
              <label className="block">
                <span className="mb-1.5 block text-xs font-semibold">Bài tập</span>
                <input
                  required
                  value={newExercise}
                  onChange={(e) => setNewExercise(e.target.value)}
                  className="field"
                  placeholder="Ví dụ: Chạy nước rút 1.200m"
                />
              </label>
              <label className="block">
                <span className="mb-1.5 block text-xs font-semibold">Mặt sân</span>
                <select
                  value={newTrack}
                  onChange={(e) => setNewTrack(e.target.value)}
                  className="field"
                >
                  <option value="Sân cỏ">Sân cỏ</option>
                  <option value="Sân cát">Sân cát</option>
                  <option value="Cổng xuất phát">Cổng xuất phát</option>
                  <option value="Hồ bơi hồi phục">Hồ bơi hồi phục</option>
                </select>
              </label>
              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  className="btn-secondary"
                  onClick={() => setNewScheduleModal(false)}
                >
                  Hủy
                </button>
                <button type="submit" className="btn-primary">
                  Tạo buổi tập
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </AppLayout>
  );
}
