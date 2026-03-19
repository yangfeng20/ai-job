// ==UserScript==
// @name         AntiAnti Hook
// @namespace    https://github.com/0xsdeo/Hook_JS
// @version      0.2
// @description  反Hook检测
// @author       0xsdeo
// @match        https://www.zhipin.com/*
// @run-at       document-start
// @icon         data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw==
// @grant        none
// ==/UserScript==

/**
 * 脚本来源：https://github.com/0xsdeo/Hook_JS
 */
(function () {
    'use strict';

    // 必须返回随时间递增的值，否则会被时序检测（t1===t2）判定为 hook 并触发内存炸弹
    const navStart = (typeof performance !== 'undefined' && performance.timing)
        ? performance.timing.navigationStart
        : Date.now();

    console.table = function () {};

    performance.now = function () {
        return Date.now() - navStart;
    };


    const methods = ['console.table', 'performance.now'];

    function initAntiAntiHook() {
        try {
            const hookedFunctions = new Map();
            for (let methodPath of methods) {
                let ref = window;
                let parts = methodPath.split('.');
                try {
                    for (let i = 0; i < parts.length - 1; i++) {
                        ref = ref[parts[i]];
                        if (!ref) break;
                    }
                    if (ref) {
                        const fn = ref[parts[parts.length - 1]];
                        if (typeof fn === 'function') {
                            hookedFunctions.set(fn, parts[parts.length - 1]);
                        }
                    }
                } catch (e) {}
            }

            if (hookedFunctions.size === 0) return;

            let temp_toString = Function.prototype.toString;
            Function.prototype.toString = function () {
                if (this === Function.prototype.toString) {
                    return 'function toString() { [native code] }';
                } else if (this === Function.prototype.constructor &&
                    hookedFunctions.has(Function.prototype.constructor)) {
                    return 'function Function() { [native code] }';
                } else if (hookedFunctions.has(this)) {
                    const funcName = hookedFunctions.get(this);
                    return `function ${funcName}() { [native code] }`;
                }
                return temp_toString.apply(this, arguments);
            };

            const hookedMethodNames = new Map();
            methods.forEach(path => {
                let ref = window;
                let parts = path.split('.');
                try {
                    for (let i = 0; i < parts.length - 1; i++) {
                        ref = ref[parts[i]];
                        if (!ref) break;
                    }
                    if (ref) {
                        const fn = ref[parts[parts.length - 1]];
                        if (typeof fn === 'function') {
                            hookedMethodNames.set(path, fn);
                        }
                    }
                } catch (e) {}
            });

            const objectHooksMap = new Map();
            methods.forEach(path => {
                const parts = path.split('.');
                const rootParts = parts[0] === 'window' ? parts.slice(1) : parts;
                if (rootParts.length < 2) return;
                const rootName = rootParts[0];
                const remaining = rootParts.slice(1);
                const fn = hookedMethodNames.get(path);
                if (!fn) return;
                if (!objectHooksMap.has(rootName)) objectHooksMap.set(rootName, []);
                objectHooksMap.get(rootName).push({ remaining, fn });
            });

            if (!objectHooksMap.has('Function')) objectHooksMap.set('Function', []);
            const existsToString = objectHooksMap.get('Function')
                .some(({ remaining }) => remaining.join('.') === 'prototype.toString');
            if (!existsToString) {
                objectHooksMap.get('Function').push({
                    remaining: ['prototype', 'toString'],
                    fn: Function.prototype.toString
                });
            }

            let property_accessor = Object.getOwnPropertyDescriptor(HTMLIFrameElement.prototype, "contentWindow");
            let get_accessor = property_accessor.get;

            Object.defineProperty(HTMLIFrameElement.prototype, "contentWindow", {
                get: function () {
                    let iframe_window = get_accessor.apply(this);

                    iframe_window = new Proxy(iframe_window, {
                        get: function (target, property, receiver) {
                            if (typeof property === 'string') {
                                for (const [fullPath, fn] of hookedMethodNames.entries()) {
                                    if (fullPath.endsWith('.' + property) ||
                                        fullPath === property) {
                                        return fn;
                                    }
                                }
                                if (objectHooksMap.has(property)) {
                                    const obj = Reflect.get(target, property, target);
                                    if (obj !== null && (typeof obj === 'object' || typeof obj === 'function')) {
                                        objectHooksMap.get(property).forEach(({ remaining, fn }) => {
                                            let ref = obj;
                                            try {
                                                for (let i = 0; i < remaining.length - 1; i++) {
                                                    ref = ref[remaining[i]];
                                                    if (!ref) return;
                                                }
                                                ref[remaining[remaining.length - 1]] = fn;
                                            } catch (e) {}
                                        });
                                    }
                                    return obj;
                                }
                            }
                            const value = Reflect.get(target, property, target);
                            if (typeof value === 'function') {
                                return value.bind(target);
                            }
                            return value;
                        },
                    });

                    return iframe_window;
                }
            });

        } catch (e) {
            console.error('AntiAnti_Hook: 初始化失败', e);
        }
    }

    initAntiAntiHook();

})();