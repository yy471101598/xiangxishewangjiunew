package com.shoppay.xxswj.bean;

/**
 * Created by songxiaotao on 2017/7/21.
 */

public class NewBoss {

    public String    MemNumber;// "5933", //新增会员人数
    public String         RechargeMoney;//": "3585025.10", //充值积分
    public String         giveMoney;//": "1074615.00", //赠送积分
    public String          firestRechargeMoney;//": "1650444.00", //首充积分
    public String           cashRechargeMoney;//": "1929130.00", //现金充值积分
    public String           bankRechargeMoney;//": "2800.00", //银联充值
    public String          cdRechargeMoney;//": "2651.00", //撤单充值
    public String          wxRechargeMoney;//": "0", //微信充值
    public String          zfbRechargeMoney;//": "0.10", //支付宝充值
    public String         orderMoney;//": "1126474.33", //订单支付积分（等于下面五种支付方式的总和）
    public String           orderCardMoney;//"1042597.13", //积分支付积分
    public String          orderCashMoney;//81319.87", //现金支付积分
    public String          orderPointMoney;//"1460.00", //酒瓶抵扣积分
    public String          orderBinkMoney;//"578.00", //银联支付积分
    public String          orderWxMoney;// "339.33", //微信支付积分

//以下All开头命名的是统计系统的总和 （上面是根据传入时间段统计）
    public String     AllMemNumber;//"106976", //会员总人数
    public String        AllRechargeMoney;// "104295669.61", //充值总积分
    public String         AllgiveMoney;//"101078398.50", //赠送总积分
    public String         AllorderMoney;//: "1136483.31" //换购总积分


}
