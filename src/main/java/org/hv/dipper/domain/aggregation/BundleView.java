package org.hv.dipper.domain.aggregation;

import org.hv.pocket.annotation.Column;
import org.hv.pocket.annotation.View;

import java.io.Serializable;

/**
 * @author wujianchuan
 */
@View
public class BundleView implements Serializable {
    private static final long serialVersionUID = 6308341762965831581L;
    @Column
    private String serviceId;
    @Column
    private String bundleId;

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getBundleId() {
        return bundleId;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }
}
