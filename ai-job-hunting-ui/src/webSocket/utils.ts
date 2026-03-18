import {mqtt} from "./mqtt";
import {protobufType} from "./protobuf"
import logger from "../logging";

export function decodeMqttAndProtobuf(data: any, caller: string): any {
    let uint8Array = new Uint8Array(data);
    let mqttDecodeResult: any;
    try {
        mqttDecodeResult = mqtt.decode(uint8Array);
        // 建立连接时的信息 or 一般为心跳数据
        if (mqttDecodeResult.topic !== 'chat') {
            logger.trace("过滤【非chat】主题", mqttDecodeResult)
            return;
        }
        let chatProtocolObj: any = protobufType.decode(mqttDecodeResult.payload);
        if (chatProtocolObj?.messages.length !== 0) {
            logger.debug(caller + "【消息mqtt】：", mqttDecodeResult)
            logger.debug(caller + "【消息对象】：", chatProtocolObj)
            // boss索要简历,交换电话，交换微信等信息时，body.type=7
            if (chatProtocolObj.messages[0].body.type === 7) {
                // 这里解码没数据，手动解码Uint8Array格式的base64数据
                chatProtocolObj.messages[0].body.text = new TextDecoder().decode(mqttDecodeResult.payload)
            }

            // 兜底解码
            if (!chatProtocolObj.messages[0].body.text && caller !== "发送") {
                chatProtocolObj.messages[0].body.text = new TextDecoder().decode(mqttDecodeResult.payload)
            }
        } else {
            logger.debug(caller + "【非消息协议对象】", chatProtocolObj)
        }
        return chatProtocolObj;
    } catch (e) {
        // 这里可能是解码失败，打印日志
        logger.debug(caller + " 解码失败", mqttDecodeResult, e)
    }
}

export function getMsgBody(data: any): string {
    let messages = data?.messages;
    if (!messages) {
        return ""
    }
    return messages?.[0]?.body?.text
}


export function normalizeNumber(number: any): number {
    if (typeof number === 'string' || typeof number === 'number') {
        return number as number;
    } else if ('low' in number) {
        return number.toNumber();
    }

    return 0;
}

