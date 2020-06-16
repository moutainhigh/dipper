package org.hv.dipper.domain.aggregation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hv.biscuits.spine.model.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wujianchuan
 */
public class UserView implements Serializable {
    private static final long serialVersionUID = -8764729476298658876L;
    private final String uuid;
    private final String avatar;
    private final String name;
    private final String departmentUuid;
    private final String departmentName;
    private String businessDepartmentUuid;
    private String businessDepartmentName;
    private final List<String> freeBundleIds;
    private final Map<String, Map<String, List<UserAuthorityView>>> departmentServiceUserAuthorityViewMap;
    private final Map<String, Map<String, List<UserAuthorityView>>> serviceDepartmentUserAuthorityViewMap;

    private UserView(String uuid, String avatar, String name, String departmentUuid, String departmentName) {
        this.uuid = uuid;
        this.avatar = avatar;
        this.name = name;
        this.departmentUuid = departmentUuid;
        this.departmentName = departmentName;
        this.freeBundleIds = new ArrayList<>();
        this.departmentServiceUserAuthorityViewMap = new HashMap<>();
        this.serviceDepartmentUserAuthorityViewMap = new HashMap<>();
    }

    /**
     * 使用{@link User}构造实例
     *
     * @param user {@link User}
     * @return user view
     */
    public static UserView fromUser(User user) {
        return new UserView(user.getUuid(), user.getAvatar(), user.getName(), user.getDepartmentUuid(), user.getDepartmentName());
    }

    /**
     * 添加权限信息
     *
     * @param userAuthorityViews 用户权限映射视图列表 {@link UserAuthorityView}
     */
    public UserView setAuthorities(List<UserAuthorityView> userAuthorityViews) {
        Map<String, Map<String, List<UserAuthorityView>>> departmentServiceUserAuthorityViewMap = userAuthorityViews.stream()
                .collect(Collectors.groupingBy(UserAuthorityView::getDepartmentUuid, Collectors.groupingBy(UserAuthorityView::getServiceId, Collectors.toList())));
        this.departmentServiceUserAuthorityViewMap.putAll(departmentServiceUserAuthorityViewMap);
        Map<String, Map<String, List<UserAuthorityView>>> serviceDepartmentUserAuthorityViewMap = userAuthorityViews.stream()
                .collect(Collectors.groupingBy(UserAuthorityView::getServiceId, Collectors.groupingBy(UserAuthorityView::getDepartmentUuid, Collectors.toList())));
        this.serviceDepartmentUserAuthorityViewMap.putAll(serviceDepartmentUserAuthorityViewMap);
        return this;
    }

    public UserView setFreeBundleIds(List<BundleView> freeBundleViews) {
        this.freeBundleIds.addAll(freeBundleViews.stream().map(BundleView::getBundleId).collect(Collectors.toList()));
        return this;
    }

    /**
     * departmentUuid->serviceId->authorityIds
     *
     * @return authority标识集合
     */
    public Map<String, Map<String, List<String>>> getDepartmentServiceAuthorityIds() {
        Map<String, Map<String, List<String>>> result = new HashMap<>(this.departmentServiceUserAuthorityViewMap.size() * 4 / 3 + 1);
        for (Map.Entry<String, Map<String, List<UserAuthorityView>>> stringMapEntry : this.departmentServiceUserAuthorityViewMap.entrySet()) {
            Map<String, List<String>> serviceAuthorityIdsMap = new HashMap<>(stringMapEntry.getValue().size() * 4 / 3 + 1);
            for (Map.Entry<String, List<UserAuthorityView>> stringListEntry : stringMapEntry.getValue().entrySet()) {
                serviceAuthorityIdsMap.put(stringListEntry.getKey(), stringListEntry.getValue().stream().map(UserAuthorityView::getAuthorityId).collect(Collectors.toList()));
            }
            result.put(stringMapEntry.getKey(), serviceAuthorityIdsMap);
        }
        return result;
    }

    /**
     * serviceId->departmentUuid->authorityIds
     *
     * @return authority标识集合
     */
    @JsonIgnore
    public Map<String, Map<String, List<String>>> getServiceDepartmentAuthorityIds() {
        Map<String, Map<String, List<String>>> result = new HashMap<>(this.serviceDepartmentUserAuthorityViewMap.size() * 4 / 3 + 1);
        for (Map.Entry<String, Map<String, List<UserAuthorityView>>> stringMapEntry : this.serviceDepartmentUserAuthorityViewMap.entrySet()) {
            Map<String, List<String>> serviceAuthorityIdsMap = new HashMap<>(stringMapEntry.getValue().size() * 4 / 3 + 1);
            for (Map.Entry<String, List<UserAuthorityView>> stringListEntry : stringMapEntry.getValue().entrySet()) {
                serviceAuthorityIdsMap.put(stringListEntry.getKey(), stringListEntry.getValue().stream().map(UserAuthorityView::getAuthorityId).collect(Collectors.toList()));
            }
            result.put(stringMapEntry.getKey(), serviceAuthorityIdsMap);
        }
        return result;
    }

    /**
     * departmentUuid->serviceId->bundleIds
     *
     * @return bundle标识集合
     */
    public Map<String, Map<String, List<String>>> getDepartmentServiceBundleIds() {
        Map<String, Map<String, List<String>>> result = new HashMap<>(this.departmentServiceUserAuthorityViewMap.size() * 4 / 3 + 1);
        for (Map.Entry<String, Map<String, List<UserAuthorityView>>> stringMapEntry : this.departmentServiceUserAuthorityViewMap.entrySet()) {
            Map<String, List<String>> serviceBundleIdsMap = new HashMap<>(stringMapEntry.getValue().size() * 4 / 3 + 1);
            for (Map.Entry<String, List<UserAuthorityView>> stringListEntry : stringMapEntry.getValue().entrySet()) {
                serviceBundleIdsMap.put(stringListEntry.getKey(), stringListEntry.getValue().stream().map(UserAuthorityView::getBundleId).distinct().collect(Collectors.toList()));
            }
            // TODO Check
            serviceBundleIdsMap.put("FREE", this.freeBundleIds);
            result.put(stringMapEntry.getKey(), serviceBundleIdsMap);
        }
        return result;
    }

    /**
     * departmentUuid->serviceId->bundleIds
     *
     * @return bundle标识集合
     */
    @JsonIgnore
    public Map<String, Map<String, List<String>>> getServiceDepartmentBundleIds() {
        Map<String, Map<String, List<String>>> result = new HashMap<>(this.serviceDepartmentUserAuthorityViewMap.size() * 4 / 3 + 1);
        for (Map.Entry<String, Map<String, List<UserAuthorityView>>> stringMapEntry : this.serviceDepartmentUserAuthorityViewMap.entrySet()) {
            Map<String, List<String>> serviceBundleIdsMap = new HashMap<>(stringMapEntry.getValue().size() * 4 / 3 + 1);
            for (Map.Entry<String, List<UserAuthorityView>> stringListEntry : stringMapEntry.getValue().entrySet()) {
                serviceBundleIdsMap.put(stringListEntry.getKey(), stringListEntry.getValue().stream().map(UserAuthorityView::getBundleId).distinct().collect(Collectors.toList()));
            }
            serviceBundleIdsMap.put("FREE", this.freeBundleIds);
            result.put(stringMapEntry.getKey(), serviceBundleIdsMap);
        }
        return result;
    }

    /**
     * @return 当前工作科室下可访问的bundle集合
     */
    public List<String> getBundleIds() {
        // TODO Free Bundles
        return this.departmentServiceUserAuthorityViewMap.getOrDefault(this.businessDepartmentUuid, new HashMap<>(0)).values().stream()
                .flatMap(Collection::stream).map(UserAuthorityView::getBundleId).distinct().collect(Collectors.toList());
    }

    /**
     * @return 当前工作科室下拥有的权限集合
     */
    public List<String> getAuthIds() {
        return this.departmentServiceUserAuthorityViewMap.getOrDefault(this.businessDepartmentUuid, new HashMap<>(0)).values().stream()
                .flatMap(Collection::stream).map(UserAuthorityView::getAuthorityId).collect(Collectors.toList());
    }

    public UserView setBusinessDepartmentUuid(String businessDepartmentUuid) {
        this.businessDepartmentUuid = businessDepartmentUuid;
        return this;
    }

    public UserView setBusinessDepartmentName(String businessDepartmentName) {
        this.businessDepartmentName = businessDepartmentName;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getName() {
        return name;
    }

    public String getDepartmentUuid() {
        return departmentUuid;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public String getBusinessDepartmentUuid() {
        return businessDepartmentUuid;
    }

    public String getBusinessDepartmentName() {
        return businessDepartmentName;
    }
}
