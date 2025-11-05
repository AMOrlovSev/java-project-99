package hexlet.code;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

public class DatabaseCleanerExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        DataSource dataSource = SpringExtension.getApplicationContext(context)
                .getBean(DataSource.class);
        cleanDatabase(dataSource);
    }

    private void cleanDatabase(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            // очищаем таблицы в правильном порядке
            statement.execute("DELETE FROM task_labels");
            statement.execute("DELETE FROM tasks");
            statement.execute("DELETE FROM labels");
            statement.execute("DELETE FROM task_statuses");
            statement.execute("DELETE FROM users");

        } catch (Exception e) {
            // Игнорируем ошибки очистки
        }
    }
}
