import logger, {Logger, LogLevel} from "./index";
import { TampermonkeyApi } from "../platform/utils";

export class LogRecorder extends Logger {
    private static readonly LOGS_STORAGE_KEY = "logs_data";
    private persistTimer: number | null = null;
    private static logs: { level: string; message: string; timestamp: string }[] = [];

    constructor(name: string = "") {
        super(name);
        this.loadLogsFromStorage();
        this.startPersistTimer();
    }

    private loadLogsFromStorage() {
        const storedLogs = TampermonkeyApi.GmGetValue(LogRecorder.LOGS_STORAGE_KEY, []);
        // 通过时间戳去重，避免重复加载历史日志
        const existingTimestamps = new Set(LogRecorder.logs.map(log => log.timestamp));
        const uniqueStoredLogs = storedLogs.filter((log: any) => !existingTimestamps.has(log.timestamp));
        LogRecorder.logs = [...LogRecorder.logs, ...uniqueStoredLogs];
    }

    private startPersistTimer() {
        this.persistTimer = window.setInterval(() => {
            this.persistLogs();
        }, 10000);
    }

    private persistLogs() {
        TampermonkeyApi.GmSetValue(LogRecorder.LOGS_STORAGE_KEY, LogRecorder.logs);
    }

    public clearLogs() {
        LogRecorder.logs = [];
        this.persistLogs();
    }

    // 设置日志存储的最大条数
    private maxLogs = 1000;

    private addLog(level: string, message: string) {
        const timestamp = new Date().toLocaleTimeString();
        LogRecorder.logs.push({ level, message, timestamp });
        if (LogRecorder.logs.length > this.maxLogs) {
            // 超过最大条数时移除最旧的一条日志
            LogRecorder.logs.shift();
        }
    }

    error(...messages: any[]) {
        const msg = messages.join(' ');
        this.addLog('error', msg);
        super.error(msg);
    }

    warn(...messages: any[]) {
        const msg = messages.join(' ');
        this.addLog('warn', msg);
        super.warn(msg);
    }

    info(...messages: any[]) {
        const msg = messages.join(' ');
        this.addLog('info', msg);
        super.info(msg);
    }

    debug(...messages: any[]) {
        const msg = messages.join(' ');
        this.addLog('debug', msg);
        super.debug(msg);
    }

    trace(...messages: any[]) {
        const msg = messages.join(' ');
        this.addLog('trace', msg);
        super.trace(msg);
    }

    // 获取日志数据，支持分页
    getLogs(page: number, pageSize: number) {
        const start = (page - 1) * pageSize;
        return LogRecorder.logs.slice(start, start + pageSize);
    }

    // 获取日志总条数
    getLogCount() {
        return LogRecorder.logs.length;
    }
}
