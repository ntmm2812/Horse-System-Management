import { createFileRoute } from "@tanstack/react-router";
import { useState } from "react";
import { Download, HeartPulse, ShieldCheck, Trophy, UserCog } from "lucide-react";
import { toast } from "sonner";
import horseImage from "@/assets/horse-profile.jpg";
import { AppLayout } from "@/components/layout/app-layout";
import { useStable } from "@/context/stable-context";

export const Route = createFileRoute("/owner")({
  head: () => ({
    meta: [
      { title: "Chủ Ngựa — Hồ sơ & Phả hệ | Mã Phong" },
      {
        name: "description",
        content: "Hồ sơ chiến mã, phả hệ ba đời, thành tích và giải thưởng qua các mùa giải.",
      },
    ],
  }),
  component: OwnerPage,
});

function OwnerPage() {
  const { addAuditLog } = useStable();
  const [season, setSeason] = useState("2025-2026");
  const [pedigreeDetail, setPedigreeDetail] = useState<string | null>(null);

  const raceHistory: [string, string, string, string, string][] =
    season === "2025-2026"
      ? [
          ["Cúp Thăng Long", "14/08/2026", "1.800m", "Hạng 1 (Vô địch)", "320 tr"],
          ["Derby Phú Thọ", "02/06/2026", "2.000m", "Hạng 2", "180 tr"],
          ["Cúp Mùa Xuân", "19/03/2026", "1.600m", "Hạng 1 (Vô địch)", "250 tr"],
          ["Grand Prix Đà Lạt", "08/12/2025", "2.400m", "Hạng 3", "90 tr"],
        ]
      : [
          ["Giải Mùa Thu 2024", "10/11/2024", "1.600m", "Hạng 1", "200 tr"],
          ["Cúp Mở Rộng", "15/07/2024", "1.800m", "Hạng 2", "120 tr"],
        ];

  const handleExportPedigree = () => {
    addAuditLog("Chủ ngựa xuất bản sao chứng nhận phả hệ chiến mã Xích Thố");
    toast.success("Đang tạo và tải xuống Chứng nhận Phả hệ Thoroughbred (PDF)...", {
      description: "Mã định danh: VN-TB-2021-0088",
    });
  };

  return (
    <AppLayout role="owner">
      <div className="mb-6 grid gap-4 sm:grid-cols-[minmax(0,1fr)_auto] sm:items-end">
        <div className="min-w-0">
          <p className="mb-1 text-xs font-bold uppercase tracking-[0.14em] text-secondary">
            Phân hệ Chủ sở hữu
          </p>
          <h1 className="font-display text-2xl font-bold sm:text-3xl">
            Hồ sơ Chiến mã & Dòng giống
          </h1>
          <p className="mt-1 max-w-2xl text-sm text-muted-foreground">
            Thông tin phả hệ, thể trạng và thành tích thi đấu của Xích Thố.
          </p>
        </div>
        <button onClick={handleExportPedigree} className="btn-primary hidden sm:inline-flex">
          <Download size={17} /> Xuất phả hệ (PDF)
        </button>
      </div>

      {/* Main Profile Card */}
      <section
        id="profile"
        className="scroll-mt-24 mb-6 rounded-lg border border-border bg-card shadow-card overflow-hidden"
      >
        <div className="grid lg:grid-cols-[360px_1fr]">
          <div className="relative min-h-[360px]">
            <img
              src={horseImage}
              loading="lazy"
              width={1200}
              height={1200}
              alt="Chân dung chiến mã Xích Thố"
              className="absolute inset-0 h-full w-full object-cover"
            />
            <div className="absolute inset-0 bg-profile-overlay" />
            <span className="absolute left-5 top-5 rounded-sm bg-hero-badge px-3 py-1.5 text-[10px] font-bold uppercase tracking-widest text-hero-foreground">
              Thoroughbred
            </span>
          </div>
          <div className="p-6 sm:p-8">
            <div className="flex flex-wrap items-start justify-between gap-4">
              <div>
                <p className="text-xs font-bold uppercase tracking-[0.18em] text-secondary">
                  VN-TB-2021-0088
                </p>
                <h2 className="mt-1 font-display text-4xl font-bold">XÍCH THỐ</h2>
                <p className="mt-2 text-sm text-muted-foreground">
                  Đực • Hạt dẻ • Sinh ngày 18/03/2021
                </p>
              </div>
              <div className="rounded-md border border-success-border bg-success-soft px-4 py-2 text-sm font-bold text-success">
                Đủ điều kiện thi đấu
              </div>
            </div>

            <div className="my-7 grid grid-cols-3 divide-x divide-border border-y border-border py-5">
              <div className="text-center">
                <strong className="font-display text-xl">5</strong>
                <p className="mt-1 text-[11px] text-muted-foreground">Tuổi</p>
              </div>
              <div className="text-center">
                <strong className="font-display text-xl">518 kg</strong>
                <p className="mt-1 text-[11px] text-muted-foreground">Cân nặng</p>
              </div>
              <div className="text-center">
                <strong className="font-display text-xl">168 cm</strong>
                <p className="mt-1 text-[11px] text-muted-foreground">Chiều cao tới vai</p>
              </div>
            </div>

            <h3 className="mb-4 font-display font-bold">Phả hệ ba đời (Nhấp để xem chi tiết)</h3>
            <div className="pedigree">
              <div
                onClick={() =>
                  setPedigreeDetail(
                    "Xích Thố: Chiến mã thế hệ thứ 3 thuần chủng Thoroughbred tại Việt Nam.",
                  )
                }
                className="pedigree-node main cursor-pointer hover:ring-2 hover:ring-primary transition"
              >
                Xích Thố<small>2021 • Bản thân</small>
              </div>
              <div className="space-y-3">
                <div
                  onClick={() =>
                    setPedigreeDetail(
                      "Frankel: Huyền thoại đua ngựa bất bại 14 trận tại Anh quốc (Dòng dõi Galileo × Kind).",
                    )
                  }
                  className="pedigree-node cursor-pointer hover:border-primary transition"
                >
                  Frankel<small>Cha • Galileo × Kind</small>
                </div>
                <div
                  onClick={() =>
                    setPedigreeDetail(
                      "Mộc Lan: Ngựa cái giống danh tiếng nhập khẩu từ Dubai (Dòng dõi Dubawi × Estrella).",
                    )
                  }
                  className="pedigree-node cursor-pointer hover:border-primary transition"
                >
                  Mộc Lan<small>Mẹ • Dubawi × Estrella</small>
                </div>
              </div>
            </div>

            {pedigreeDetail && (
              <div className="mt-4 flex items-center justify-between rounded-md bg-primary-soft p-3 text-xs text-primary animate-fade-in">
                <span>{pedigreeDetail}</span>
                <button onClick={() => setPedigreeDetail(null)} className="ml-2 font-bold">
                  ×
                </button>
              </div>
            )}

            <div className="mt-6 flex items-center gap-2 text-xs text-muted-foreground">
              <UserCog size={15} /> Chủ sở hữu: Phạm Gia Huy • HLV Trưởng: Nguyễn Văn A
            </div>
          </div>
        </div>
      </section>

      <div className="grid gap-6 xl:grid-cols-[1.2fr_0.8fr]">
        {/* Race History */}
        <section
          id="races"
          className="scroll-mt-24 rounded-lg border border-border bg-card shadow-card"
        >
          <div className="grid grid-cols-[minmax(0,1fr)_auto] items-start gap-4 border-b border-border px-5 py-4">
            <div className="flex min-w-0 items-start gap-3">
              <span className="mt-0.5 grid size-8 shrink-0 place-items-center rounded-md bg-primary-soft text-primary">
                <Trophy size={17} />
              </span>
              <div>
                <h2 className="font-display text-base font-bold">Lịch sử thi đấu & Tiền thưởng</h2>
                <p className="text-xs text-muted-foreground">
                  Tổng tiền thưởng mùa: 840 triệu đồng
                </p>
              </div>
            </div>
            <select
              value={season}
              onChange={(e) => setSeason(e.target.value)}
              className="field h-8 py-0 text-xs"
            >
              <option value="2025-2026">Mùa giải 2025–2026</option>
              <option value="2024-2025">Mùa giải 2024–2025</option>
            </select>
          </div>
          <div className="overflow-x-auto">
            <table>
              <thead>
                <tr>
                  <th>Giải đấu</th>
                  <th>Ngày</th>
                  <th>Cự ly</th>
                  <th>Hạng</th>
                  <th>Tiền thưởng</th>
                </tr>
              </thead>
              <tbody>
                {raceHistory.map((r) => (
                  <tr key={r[0]}>
                    <td className="font-semibold">{r[0]}</td>
                    <td>{r[1]}</td>
                    <td>{r[2]}</td>
                    <td>
                      <span className={r[3].includes("Hạng 1") ? "text-primary font-bold" : ""}>
                        {r[3]}
                      </span>
                    </td>
                    <td className="font-bold text-success">{r[4]}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>

        {/* Health status */}
        <section
          id="health"
          className="scroll-mt-24 rounded-lg border border-border bg-card shadow-card"
        >
          <div className="grid grid-cols-[minmax(0,1fr)_auto] items-start gap-4 border-b border-border px-5 py-4">
            <div className="flex min-w-0 items-start gap-3">
              <span className="mt-0.5 grid size-8 shrink-0 place-items-center rounded-md bg-primary-soft text-primary">
                <HeartPulse size={17} />
              </span>
              <div>
                <h2 className="font-display text-base font-bold">Trạng thái thể lực & Sức khỏe</h2>
                <p className="text-xs text-muted-foreground">Đồng bộ tự động từ phòng khám thú y</p>
              </div>
            </div>
          </div>
          <div className="space-y-6 p-5">
            <div>
              <div className="mb-2 flex justify-between text-sm">
                <span className="font-medium">Sẵn sàng thi đấu</span>
                <strong>92%</strong>
              </div>
              <div className="h-2 overflow-hidden rounded-full bg-muted">
                <div className="h-full rounded-full bg-primary" style={{ width: "92%" }} />
              </div>
            </div>

            <div>
              <div className="mb-2 flex justify-between text-sm">
                <span className="font-medium">Thể lực hiện tại</span>
                <strong>87%</strong>
              </div>
              <div className="h-2 overflow-hidden rounded-full bg-muted">
                <div className="h-full rounded-full bg-primary" style={{ width: "87%" }} />
              </div>
            </div>

            <div>
              <div className="mb-2 flex justify-between text-sm">
                <span className="font-medium">Phục hồi cơ bắp</span>
                <strong>78%</strong>
              </div>
              <div className="h-2 overflow-hidden rounded-full bg-muted">
                <div className="h-full rounded-full bg-primary" style={{ width: "78%" }} />
              </div>
            </div>

            <div>
              <div className="mb-2 flex justify-between text-sm">
                <span className="font-medium">Cân bằng dinh dưỡng</span>
                <strong>95%</strong>
              </div>
              <div className="h-2 overflow-hidden rounded-full bg-muted">
                <div className="h-full rounded-full bg-primary" style={{ width: "95%" }} />
              </div>
            </div>

            <div className="rounded-md bg-success-soft p-4 text-xs leading-relaxed text-success flex items-center gap-2">
              <ShieldCheck size={18} className="shrink-0" />
              <span>
                Không có chấn thương đang điều trị. Lịch khám định kỳ tiếp theo: 28/09/2026.
              </span>
            </div>
          </div>
        </section>
      </div>
    </AppLayout>
  );
}
