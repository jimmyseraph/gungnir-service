package vip.testops.gungnir.user.commons;

public enum BaseCodeEnum {
    COMMON_SUCCESS(1000, "success", "成功"),
    PARAM_MISS(2001, "required parameters[%s] are missing", "缺少必要的参数[%s]"),
    PARAM_ILLEGAL(2002, "required parameters[%s] are illegal", "参数[%s]不合法"),
    SERVICE_ERROR(3001, "service error: %s", "服务异常：%s");

    private Integer code;
    private String descEN;
    private String descCN;

    BaseCodeEnum(Integer code, String descEN, String descCN){
        this.code = code;
        this.descEN = descEN;
        this.descCN = descCN;
    }

    public static BaseCodeEnum getByCode(Integer code) {
        if(code == null) {
            return null;
        } else {
            BaseCodeEnum target = null;
            BaseCodeEnum[] enums = values();
            for(int i = 0; i < enums.length; i++){
                BaseCodeEnum baseCodeEnum = enums[i];
                if(baseCodeEnum.getCode() == code) {
                    target = baseCodeEnum;
                }
            }
            return target;
        }
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDescEN() {
        return descEN;
    }

    public void setDescEN(String descEN) {
        this.descEN = descEN;
    }

    public String getDescCN() {
        return descCN;
    }

    public void setDescCN(String descCN) {
        this.descCN = descCN;
    }

}
