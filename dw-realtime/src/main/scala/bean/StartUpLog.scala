package bean

/**
 * 启动日志 样例类信息
 * @param mid  设备唯一标识
 * @param uid  用户标识
 * @param appid  应用id
 * @param area  地区
 * @param os  操作系统
 * @param ch  应用市场
 * @param type  日志类型
 * @param logHour  日志小时
 * @param logHourMinute 日志时分
 * @param ts  日志时间戳
 */
case class StartUpLog(
                       mid: String,
                       uid: String,
                       appid: String,
                       area: String,
                       os: String,
                       ch: String,
                       `type`: String,
                       vs:String,
                       var logDate:String, //yyyy-MM-dd
                       var logHour: String, //HH
                       var logHourMinute: String, //HH:mm
                       var ts: Long
                     )
