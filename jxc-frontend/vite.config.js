import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'
import path from 'path'

export default defineConfig({
    base: process.env.VITE_BASE_PATH || '/',
    plugins: [vue(), tailwindcss()],
    resolve: {
        alias: {
            '@': path.resolve(__dirname, './src'),
            '@/lib': path.resolve(__dirname, './src/lib'),
        }
    },
    server: {
        port: 5173,
        strictPort: true,
        // 内网穿透（cloudflared 快速隧道）访问时 Host 头是 trycloudflare 域名，
        // Vite 6 默认拦截非本机 Host，这里只放行隧道域，不放开任意域名
        allowedHosts: ['.trycloudflare.com']
    },
    preview: {
        port: 4173,
        allowedHosts: ['.trycloudflare.com']
    }
})
