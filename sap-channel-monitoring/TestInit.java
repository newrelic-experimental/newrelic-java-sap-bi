// Quick test to verify the synchronization fix
import com.newrelic.instrumentation.labs.sap.channel.monitoring.ChannelMonitoringLogger;

public class TestInit {
    public static void main(String[] args) {
        System.out.println("Testing ChannelMonitoringLogger initialization...");
        
        try {
            // This should not throw NullPointerException anymore
            ChannelMonitoringLogger.init();
            System.out.println("✅ Initialization successful - no NullPointerException!");
        } catch (Exception e) {
            System.out.println("❌ Error during initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
}