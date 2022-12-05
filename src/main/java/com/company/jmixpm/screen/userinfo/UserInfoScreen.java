package com.company.jmixpm.screen.userinfo;

import com.company.jmixpm.app.PostService;
import com.company.jmixpm.entity.UserInfo;
import com.google.common.collect.ImmutableMap;
import io.jmix.core.LoadContext;
import io.jmix.core.common.util.Preconditions;
import io.jmix.ui.action.Action;
import io.jmix.ui.model.InstanceLoader;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.navigation.UrlIdSerializer;
import io.jmix.ui.navigation.UrlParamsChangedEvent;
import io.jmix.ui.navigation.UrlRouting;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route("user-info")
@UiController("UserInfoScreen")
@UiDescriptor("user-info-screen.xml")
public class UserInfoScreen extends Screen {
    @Autowired
    private InstanceLoader<UserInfo> userInfoDl;

    @Autowired
    private PostService postService;
    @Autowired
    private UrlRouting urlRouting;

    private Long userId;

    public UserInfoScreen withUserId(Long userId) {
        Preconditions.checkNotNullArgument(userId);
        this.userId = userId;

        return this;
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        String serializeId = UrlIdSerializer.serializeId(userId);
        urlRouting.replaceState(this, ImmutableMap.of("id", serializeId));
    }

    @Subscribe
    public void onUrlParamsChanged(UrlParamsChangedEvent event) {
        String serializedId = event.getParams().get("id");
        userId = ((Long) UrlIdSerializer.deserializeId(Long.class, serializedId));

        userInfoDl.load();
    }

    @Install(to = "userInfoDl", target = Target.DATA_LOADER)
    private UserInfo userInfoDlLoadDelegate(LoadContext<UserInfo> loadContext) {
        UserInfo userInfo = postService.fetchUserInfo(userId);
        if (userInfo == null) {
            throw new IllegalStateException("User wasn't loaded for Id: " + userId);
        }

        return userInfo;
    }



    @Subscribe("windowClose")
    public void onWindowClose(Action.ActionPerformedEvent event) {
        closeWithDefaultAction();
    }
}