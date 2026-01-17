'use client';

import { Zap } from 'lucide-react';

export function Header() {
    return (
        <header className="sticky top-0 z-50 w-full border-b border-zinc-800 bg-black/80 backdrop-blur-xl">
            <div className="container mx-auto px-4 h-16 flex items-center justify-between">
                <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-emerald-400 to-cyan-500 flex items-center justify-center">
                        <Zap className="w-6 h-6 text-black" />
                    </div>
                    <div>
                        <h1 className="text-xl font-bold text-white tracking-tight">
                            Gadget Info
                        </h1>
                        <p className="text-xs text-zinc-500">最新ガジェット情報</p>
                    </div>
                </div>

                <nav className="flex items-center gap-4">
                    <span className="text-xs text-zinc-500 hidden sm:block">
                        迷わない、見逃さない、未来に触れる
                    </span>
                </nav>
            </div>
        </header>
    );
}
