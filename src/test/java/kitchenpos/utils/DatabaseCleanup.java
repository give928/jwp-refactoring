package kitchenpos.utils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;
import javax.persistence.metamodel.EntityType;
import java.util.List;
import java.util.stream.Collectors;

@Service
@ActiveProfiles("test")
public class DatabaseCleanup implements InitializingBean {
    @PersistenceContext
    private EntityManager entityManager;

    private List<DatabaseTable> databaseTables;

    @Override
    public void afterPropertiesSet() {
        databaseTables = entityManager.getMetamodel().getEntities().stream()
                .filter(e -> e.getJavaType().getAnnotation(Entity.class) != null)
                .map(this::mapDatabaseTable)
                .collect(Collectors.toList());
    }

    @Transactional
    public void execute() {
        entityManager.flush();
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();

        for (DatabaseTable databaseTable : databaseTables) {
            entityManager.createNativeQuery(String.format("TRUNCATE TABLE %s", databaseTable.getTableName())).executeUpdate();
            entityManager.createNativeQuery(
                    String.format("ALTER TABLE %s ALTER COLUMN %s RESTART WITH 1", databaseTable.getTableName(), databaseTable.getPrimaryKeyName())).executeUpdate();
        }

        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    }

    private DatabaseTable mapDatabaseTable(EntityType<?> e) {
        return new DatabaseTable(camelToSnake(getTableName(e)), getPrimaryKeyName(e));
    }

    private String getTableName(EntityType<?> e) {
        System.out.println("e.getIdType() = " + e.getId(e.getIdType().getJavaType()).getName());
        if (e.getJavaType().getAnnotation(Table.class) != null && e.getJavaType().getAnnotation(Table.class).name() != null) {
            return e.getJavaType().getAnnotation(Table.class).name();
        }
        return e.getName();
    }

    private String getPrimaryKeyName(EntityType<?> e) {
        return e.getId(e.getIdType().getJavaType()).getName();
    }

    public static String camelToSnake(String str) {
        char c = str.charAt(0);
        StringBuilder result = new StringBuilder(String.valueOf(Character.toLowerCase(c)));
        for (int i = 1; i < str.length(); i++) {
            appendCharacter(result, str.charAt(i));
        }
        return result.toString();
    }

    private static void appendCharacter(StringBuilder result, char ch) {
        if (Character.isUpperCase(ch)) {
            result.append('_');
            result.append(Character.toLowerCase(ch));
            return;
        }
        result.append(ch);
    }

    static class DatabaseTable {
        private final String tableName;
        private final String primaryKeyName;

        public DatabaseTable(String tableName, String primaryKeyName) {
            this.tableName = tableName;
            this.primaryKeyName = primaryKeyName;
        }

        public String getTableName() {
            return tableName;
        }

        public String getPrimaryKeyName() {
            return primaryKeyName;
        }
    }
}
