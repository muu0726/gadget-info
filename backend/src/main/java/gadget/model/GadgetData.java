package gadget.model;

import java.time.Instant;
import java.util.List;

/**
 * ガジェットデータ全体を格納するコンテナ
 */
public class GadgetData {
    private List<Gadget> gadgets;
    private String lastUpdated;

    public GadgetData() {
        this.lastUpdated = Instant.now().toString();
    }

    public GadgetData(List<Gadget> gadgets) {
        this.gadgets = gadgets;
        this.lastUpdated = Instant.now().toString();
    }

    public List<Gadget> getGadgets() { return gadgets; }
    public void setGadgets(List<Gadget> gadgets) { this.gadgets = gadgets; }

    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }
}
