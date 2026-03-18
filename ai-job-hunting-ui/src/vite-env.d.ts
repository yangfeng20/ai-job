/// <reference types="vite/client" />
/// <reference types="vite-plugin-monkey/client" />
//// <reference types="vite-plugin-monkey/global" />
// 用于ts使用vue不能识别问题
declare module '*.vue' {
    import type {DefineComponent} from 'vue'
    const component: DefineComponent<{}, {}, any>
    export default component
}

declare module 'event-source-polyfill';


// 支持md识别
declare module '*.md' {
    import {DefineComponent} from 'vue';
    const Component: DefineComponent<{}, {}, any>;
    export default Component;
}