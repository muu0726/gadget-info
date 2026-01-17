export function Footer() {
    return (
        <footer className="border-t border-zinc-800 bg-black py-8">
            <div className="container mx-auto px-4">
                <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
                    <p className="text-sm text-zinc-500">
                        © 2026 Gadget Info. All rights reserved.
                    </p>
                    <div className="flex items-center gap-4 text-sm text-zinc-500">
                        <span>Powered by Gemini AI</span>
                        <span>•</span>
                        <span>GitHub Pages</span>
                    </div>
                </div>
            </div>
        </footer>
    );
}
