import {isProdEnv} from "./tools";
import {EventSourcePolyfill as ESP} from "event-source-polyfill";
import EventSourcePolyfillDefault from "event-source-polyfill";

const EventSourcePolyfill = isProdEnv() ? EventSourcePolyfillDefault : ESP;

export class SSEClient {
    private eventSource: any | null;

    private callbackList: any[];

    constructor(private url: string) {
        this.eventSource = null;
        this.callbackList = [];
    }

    public start(): void {
        let authorization = localStorage.getItem('Authorization');
        this.eventSource = new EventSourcePolyfill(this.url, {
            withCredentials: true,
            // 5分钟超时(略小于nginx sse超时时间)
            heartbeatTimeout: 5 * 59 * 1000,
            headers: {
                'Authorization': authorization
            }
        } as any);
        this.eventSource.onmessage = (event: MessageEvent) => {
            // 调用回调函数
            this.callbackList.forEach(callBackFunc => callBackFunc(event))
        };
        this.eventSource.onerror = (error: Event) => {
            this.close();
        };
    }

    public addEventListener(eventType: string, listener: EventListener): void {
        if (this.eventSource) {
            this.eventSource.addEventListener(eventType, listener);
        }
    }

    public addOnMsgCallback(func: Function) {
        this.callbackList.push(func)
    }

    public close(): void {
        if (this.eventSource) {
            this.eventSource.close();
            this.eventSource = null;
        }
    }
}

// 使用示例
// const sseClient = new SSEClient('http://api/sse/connect');
// sseClient.addOnMsgCallback(() => {
//
// })
// sseClient.start();
//
// // 如果需要，可以添加自定义事件监听器
// sseClient.addEventListener('自定义事件类型', (event: Event) => {
//     // 处理事件
// });
