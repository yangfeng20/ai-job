import axios from "../axios";


export class AiPower {

    public static async ask(question: string, jobKey: string, bossUserInfo: any): Promise<any> {
        return axios.post("/api/job/seeker/cloned/ask", {
                question: question,
                jobKey: jobKey,
                jobInfo: {
                    // 完整的title需要调用getBossData接口获取，代价较大; 这暂时用jobTitle代替
                    jobTitle: bossUserInfo.jobTitle,
                }
            },
            {
                // ask接口超时时间设为30s；部分ai回复较慢
                timeout: 90000
            })
    }
    public static async filter(prompt: string, jobBaseInfo: string, jobExtInfo: string): Promise<any> {
        return axios.post("api/job/filter/one", {
                prompt: prompt,
                jobBaseInfo: jobBaseInfo,
                jobExtInfo: jobExtInfo
            },
            {
                // 部分ai回复较慢
                timeout: 60000
            })
    }

    public static async updateAskStatus(jobKey: string, stop: boolean): Promise<any> {
        return axios.post(`/api/job/seeker/cloned/change/session/status?jobKey=${jobKey}&stop=${stop}`)
    }
}