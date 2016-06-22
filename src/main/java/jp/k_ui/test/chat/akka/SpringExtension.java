package jp.k_ui.test.chat.akka;

import org.springframework.context.ApplicationContext;

import akka.actor.AbstractExtensionId;
import akka.actor.ExtendedActorSystem;
import akka.actor.Extension;
import akka.actor.ExtensionId;
import akka.actor.ExtensionIdProvider;
import lombok.Getter;

public class SpringExtension extends AbstractExtensionId<SpringExtension.SpringExtensionImpl>
        implements ExtensionIdProvider {
    public static final SpringExtension SpringExtensionProvider = new SpringExtension();

    @Override
    public SpringExtensionImpl createExtension(ExtendedActorSystem extendedActorSystem) {
        return new SpringExtensionImpl();
    }

    @Override
    public ExtensionId<? extends Extension> lookup() {
        return SpringExtensionProvider;
    }

    public static class SpringExtensionImpl implements Extension {
        @Getter
        private ApplicationContext applicationContext;

        public void init(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }
    }
}
