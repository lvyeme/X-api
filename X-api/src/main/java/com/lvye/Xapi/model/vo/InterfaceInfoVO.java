package com.lvye.Xapi.model.vo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.*;
import com.lvye.Xapi.model.entity.InterfaceInfo;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 接口封装类
 * 接口信息
 * @TableName interface_info
 */
@TableName(value ="interface_info")
@Data
public class InterfaceInfoVO implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 接口地址
     */
    private String url;

    /**
     * 请求头
     */
    private String requestHeader;

    /**
     * 响应头
     */
    private String responseHeader;

    /**
     * 接口状态（0-关闭，1-开启）
     */
    private Integer status;

    /**
     * 请求类型
     */
    private String method;

    /**
     * 创建人
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 创建接口人的信息
     */
    private UserVO userVO;

    /**
     * 包装类转对象
     *
     * @param interfaceinfoVO
     * @return
     */
    public static InterfaceInfo voToObj(InterfaceInfoVO interfaceinfoVO) {
        if (interfaceinfoVO == null) {
            return null;
        }
        InterfaceInfo interfaceinfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceinfoVO, interfaceinfo);
        return interfaceinfo;
    }

    /**
     * 对象转包装类
     *
     * @param interfaceinfo
     * @return
     */
    public static InterfaceInfoVO objToVo(InterfaceInfo interfaceinfo) {
        if (interfaceinfo == null) {
            return null;
        }
        InterfaceInfoVO interfaceinfoVO = new InterfaceInfoVO();
        BeanUtils.copyProperties(interfaceinfo, interfaceinfoVO);
        return interfaceinfoVO;
    }

    private static final long serialVersionUID = 1L;
}