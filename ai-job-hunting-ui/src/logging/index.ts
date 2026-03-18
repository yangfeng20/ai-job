export enum LogLevel {
    Error = 1,
    Warn,
    Info,
    Debug,
    Trace,
    OriginalTrace,
}

let globalLogLevel: LogLevel = LogLevel.Info;
const loggerInstances: Logger[] = [];
const logStyles = {
    // 30 - 黑色
    // 31 - 红色
    // 32 - 绿色
    // 33 - 黄色
    // 34 - 蓝色
    // 35 - 紫色
    // 36 - 青色
    // 37 - 白色
    error: '\x1b[31m%s\x1b[0m', // 红色
    warn: '\x1b[33m%s\x1b[0m', // 黄色
    info: '\x1b[32m%s\x1b[0m', // 绿色
    debug: '\x1b[36m%s\x1b[0m', // 青色
    trace: '\x1b[34m%s\x1b[0m'  // 蓝色
};

export class Logger {
    static rootLogger: Logger = new Logger("root");

    private readonly name: string;
    private logLevel: LogLevel;

    constructor(name: string = "", logLevel: LogLevel = globalLogLevel) {
        this.name = name;
        this.logLevel = logLevel;
        loggerInstances.push(this)
    }

    public static setGlobalLogLevel(logLevel: LogLevel): void {
        globalLogLevel = logLevel;
        loggerInstances.forEach(logger => logger.setLogLevel(logLevel));
    }

    setLogLevel(logLevel: LogLevel): void {
        this.logLevel = logLevel;
    }

    getLogLevel(): LogLevel {
        return this.logLevel;
    }

    public error(...messages: any[]): void {
        if (this.logLevel >= LogLevel.Error) {
            console.error(logStyles.error, `[${this.name}][ERROR]`, ...messages);
        }
    }

    public warn(...messages: any[]): void {
        if (this.logLevel >= LogLevel.Warn) {
            console.warn(logStyles.warn, `[${this.name}][WARN]`, ...messages);
        }
    }

    public info(...messages: any[]): void {
        if (this.logLevel >= LogLevel.Info) {
            console.log(logStyles.info, `[${this.name}][INFO]`, ...messages);
        }
    }

    public debug(...messages: any[]): void {
        if (this.logLevel >= LogLevel.Debug) {
            console.debug(logStyles.debug, `[${this.name}][DEBUG]`, ...messages);
        }
    }

    public trace(...messages: any[]): void {
        if (this.logLevel >= LogLevel.Trace) {
            console.debug(logStyles.trace, `[${this.name}][TRACE]`, ...messages);
        }
    }
    public originalTrace(...messages: any[]): void {
        if (this.logLevel >= LogLevel.OriginalTrace) {
            console.trace(logStyles.trace, `[${this.name}][ORIGINAL_TRACE]`, ...messages);
        }
    }
}

export default Logger.rootLogger as Logger;
