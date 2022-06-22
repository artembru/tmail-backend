package com.linagora.tmail.combined.identity;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.james.adapter.mailbox.DelegationStoreAuthorizator;
import org.apache.james.backends.cassandra.components.CassandraModule;
import org.apache.james.mailbox.Authorizator;
import org.apache.james.server.core.configuration.ConfigurationProvider;
import org.apache.james.user.api.DelegationStore;
import org.apache.james.user.api.UsersRepository;
import org.apache.james.user.cassandra.CassandraDelegationStore;
import org.apache.james.user.cassandra.CassandraRepositoryConfiguration;
import org.apache.james.user.cassandra.CassandraUsersDAO;
import org.apache.james.user.cassandra.CassandraUsersRepositoryModule;
import org.apache.james.user.ldap.LdapRepositoryConfiguration;
import org.apache.james.user.ldap.ReadOnlyLDAPUsersDAO;
import org.apache.james.user.ldap.ReadOnlyUsersLDAPRepository;
import org.apache.james.user.lib.UsersDAO;
import org.apache.james.utils.InitializationOperation;
import org.apache.james.utils.InitilizationOperationBuilder;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.multibindings.ProvidesIntoSet;

public class CombinedUsersRepositoryModule extends AbstractModule {
    @Override
    public void configure() {
        Multibinder<CassandraModule> cassandraDataDefinitions = Multibinder.newSetBinder(binder(), CassandraModule.class);
        cassandraDataDefinitions.addBinding().toInstance(CassandraUsersRepositoryModule.MODULE);

        bind(CassandraUsersDAO.class).in(Scopes.SINGLETON);
        bind(ReadOnlyLDAPUsersDAO.class).in(Scopes.SINGLETON);
        bind(CombinedUserDAO.class).in(Scopes.SINGLETON);
        bind(CombinedUsersRepository.class).in(Scopes.SINGLETON);

        bind(UsersDAO.class).to(CombinedUserDAO.class);
        bind(UsersRepository.class).to(CombinedUsersRepository.class);
        bind(DelegationStore.class).to(CassandraDelegationStore.class);
        bind(Authorizator.class).to(DelegationStoreAuthorizator.class);
    }

    @Provides
    @Singleton
    public LdapRepositoryConfiguration provideConfiguration(ConfigurationProvider configurationProvider) throws ConfigurationException {
        return LdapRepositoryConfiguration.from(
            configurationProvider.getConfiguration("usersrepository"));
    }

    @Provides
    @Singleton
    public CassandraRepositoryConfiguration provideCassandraRepositoryConfiguration() {
        return CassandraRepositoryConfiguration.DEFAULT;
    }

    @ProvidesIntoSet
    InitializationOperation configureUsersRepository(ConfigurationProvider configurationProvider, CombinedUsersRepository usersRepository) {
        return InitilizationOperationBuilder
            .forClass(CombinedUsersRepository.class)
            .init(() -> usersRepository.configure(configurationProvider.getConfiguration("usersrepository")));
    }

    @ProvidesIntoSet
    InitializationOperation configureLdap(LdapRepositoryConfiguration configuration, ReadOnlyLDAPUsersDAO readOnlyLDAPUsersDAO) {
        return InitilizationOperationBuilder
            .forClass(ReadOnlyUsersLDAPRepository.class)
            .init(() -> {
                readOnlyLDAPUsersDAO.configure(configuration);
                readOnlyLDAPUsersDAO.init();
            });
    }
}
