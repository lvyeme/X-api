package com.lvye.Xapi.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvye.Xapi.common.ErrorCode;
import com.lvye.Xapi.constant.CommonConstant;
import com.lvye.Xapi.exception.BusinessException;
import com.lvye.Xapi.exception.ThrowUtils;
import com.lvye.Xapi.model.dto.interfaceInfo.InterfaceInfoQueryRequest;
import com.lvye.Xapi.model.entity.*;
import com.lvye.Xapi.model.vo.InterfaceInfoVO;
import com.lvye.Xapi.model.vo.UserVO;
import com.lvye.Xapi.service.InterfaceInfoService;
import com.lvye.Xapi.mapper.InterfaceInfoMapper;
import com.lvye.Xapi.service.UserService;
import com.lvye.Xapi.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author 老子
* @description 针对表【interface_info(接口信息)】的数据库操作Service实现
* @createDate 2024-04-15 09:39:46
*/
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
    implements InterfaceInfoService{
    @Resource
    private UserService userService;

    /**
     * 校验接口是否合法
     * @param interfaceInfo
     * @param add
     */
    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add) {
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String name = interfaceInfo.getName();
        String description = interfaceInfo.getDescription();
        String url = interfaceInfo.getUrl();
        //todo 接口信息
        String requestHeader = interfaceInfo.getRequestHeader();
        String responseHeader = interfaceInfo.getResponseHeader();
        String method = interfaceInfo.getMethod();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(url, name, description), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(name) && name.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "名字过长");
        }
        if (StringUtils.isNotBlank(url) && url.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "连接过长");
        }
        if (StringUtils.isNotBlank(description) && description.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口描述过长");
        }
    }

    /**
     * 获取查询包装类（考虑用户会根据那些字段查询，根据前端传来的请求对象，得到，mybatis 框架
     * 支持的查询 QueryWrapper 类）
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<InterfaceInfo> getQueryWrapper(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        if (interfaceInfoQueryRequest == null) {
            return queryWrapper;
        }
        Long id = interfaceInfoQueryRequest.getId();
        String name = interfaceInfoQueryRequest.getName();
        String description = interfaceInfoQueryRequest.getDescription();
        String url = interfaceInfoQueryRequest.getUrl();
        String requestHeader = interfaceInfoQueryRequest.getRequestHeader();
        String responseHeader = interfaceInfoQueryRequest.getResponseHeader();
        String method = interfaceInfoQueryRequest.getMethod();
        Long userId = interfaceInfoQueryRequest.getUserId();
        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.like(StringUtils.isNotBlank(url), "url", url);
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);

        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取查询的封装类
     * @param interfaceInfo
     * @param request
     * @return
     */
    @Override
    public InterfaceInfoVO getInterfaceInfoVO(InterfaceInfo interfaceInfo, HttpServletRequest request) {
        InterfaceInfoVO interfaceInfoVO = InterfaceInfoVO.objToVo(interfaceInfo);
        // 1. 关联查询用户信息
        Long userId = interfaceInfo.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        interfaceInfoVO.setUserVO(userVO);
        return interfaceInfoVO;
    }

    @Override
    public Page<InterfaceInfoVO> getInterfaceInfoVOPage(Page<InterfaceInfo> interfaceInfoPage, HttpServletRequest request) {
        List<InterfaceInfo> interfaceInfoList = interfaceInfoPage.getRecords();
        Page<InterfaceInfoVO> interfaceInfoVOPage = new Page<>(interfaceInfoPage.getCurrent(), interfaceInfoPage.getSize(), interfaceInfoPage.getTotal());
        if (CollUtil.isEmpty(interfaceInfoList)) {
            return interfaceInfoVOPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = interfaceInfoList.stream().map(InterfaceInfo::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        // 填充信息
        List<InterfaceInfoVO> interfaceInfoVOList = interfaceInfoList.stream().map(interfaceInfo -> {
            InterfaceInfoVO interfaceInfoVO = InterfaceInfoVO.objToVo(interfaceInfo);
            Long userId = interfaceInfo.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            interfaceInfoVO.setUserVO(userService.getUserVO(user));
            return interfaceInfoVO;
        }).collect(Collectors.toList());
        interfaceInfoVOPage.setRecords(interfaceInfoVOList);
        return interfaceInfoVOPage;
    }

}




