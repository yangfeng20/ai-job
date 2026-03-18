import {defineConfig} from 'vite';
import vue from '@vitejs/plugin-vue';
import Markdown from 'vite-plugin-md';
import monkey, {cdn, util} from "vite-plugin-monkey";
import AutoImport from 'unplugin-auto-import/vite';
import Components from 'unplugin-vue-components/vite';
import {ElementPlusResolver} from 'unplugin-vue-components/resolvers';

let matchUrlList: string[] = [
    'https://www.zhipin.com/web/geek/*',
    'https://www.zhipin.com/overseas/*'
];

// https://vitejs.dev/config/
// https://github.com/lisonge/vite-plugin-monkey
export default defineConfig(({mode}) => {
    const isProduction = mode === 'production';

    const plugins = [
        vue({
            include: [/\.vue$/, /\.md$/], // Support .vue and .md files
        }),
        Markdown(),
        AutoImport({
            resolvers: [ElementPlusResolver()],
        }),
        Components({
            resolvers: [ElementPlusResolver()],
        }),
        monkey({
            entry: 'src/main.ts',
            userscript: {
                name: "AI工作猎手-让ai帮您找工作！",
                author: "maple.",
                version: '0.0.24-beta',
                license: 'Apache License 2.0',
                icon: 'https://gitee.com/yangfeng20/ai-job/raw/master/file/icon.png',
                description: "找工作，用AI工作猎手！让AI帮您找工作！ai坐席：【DeepSeek+ChatGpt】赋能，ai助理作为您的求职者分身24小时 * 7在线找工作，并结合您的简历信息定制化回复。批量投递，自动发送简历，交换联系方式。hr拒绝挽留。高意向邮件通知，让您不错过每一份工作机会。BOSS直聘",
                namespace: 'https://github.com/yangfeng20',
                connect: ["docdownload.zhipin.com"],
                updateURL: "https://gitee.com/yangfeng20/ai-job/raw/master/ai-job-hunting.user.js",
                downloadURL: "https://gitee.com/yangfeng20/ai-job/raw/master/ai-job-hunting.user.js",
                match: matchUrlList,
            },
            build: {
                externalGlobals: {
                    vue: cdn.jsdelivr('Vue', 'dist/vue.global.prod.js')
                        .concat('https://unpkg.com/vue-demi@latest/lib/index.iife.js')
                        .concat(util.dataUrl(";window.Vue=Vue;")),
                    "element-plus": cdn.jsdelivr("ElementPlus", "dist/index.full.min.js"),
                    protobufjs: cdn.jsdelivr("protobuf", "dist/protobuf.min.js"),
                    pinia: cdn.jsdelivr("Pinia", "dist/pinia.iife.prod.js"),
                    "event-source-polyfill": cdn.jsdelivr("EventSourcePolyfill", "src/eventsource.min.js"),
                },
                externalResource: {
                    "element-plus/dist/index.css": cdn.jsdelivr(),
                    "element-plus/theme-chalk/dark/css-vars.css": cdn.jsdelivr(),
                },
            },
        })
    ];

    return {
        plugins,
        resolve: {
            extensions: ['.js', '.ts', '.vue', '.json', '.css'],
            alias: {
                'vue': 'vue/dist/vue.esm-bundler.js'
            }
        },
    };
});