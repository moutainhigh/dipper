package org.hv.dipper.domain.aggregation;

import org.hv.pocket.annotation.Column;
import org.hv.pocket.annotation.View;

import java.io.Serializable;

/**
 * @author wujianchuan
 */
@View
public class AuthorityView extends BundleView implements Serializable {
    private static final long serialVersionUID = 6308341762965831581L;
    @Column
    private String actionId;
    @Column
    private String authorityId;

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public String getAuthorityId() {
        return authorityId;
    }

    public void setAuthorityId(String authorityId) {
        this.authorityId = authorityId;
    }
}
