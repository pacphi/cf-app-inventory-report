package io.pivotal.cfapp.repository.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import org.davidmoten.rx.jdbc.Database;
import org.davidmoten.rx.jdbc.exceptions.SQLRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;


@Profile("jdbc")
@Component
public class DatabaseCreator implements ApplicationRunner {

	private final Database database;
	
	@Autowired
	public DatabaseCreator(Database database) {
		this.database = database;
		
	}
	@Override
	public void run(ApplicationArguments args) throws Exception {
		try {
			Connection c = database.connection().blockingGet();
            c.setAutoCommit(true);
            c.prepareStatement("create table app_detail ( id int identity primary key, organization varchar(100), space varchar(100), app_name varchar(100), buildpack varchar(50), image varchar(250), stack varchar(25), running_instances int, total_instances int, urls varchar(1000), last_pushed timestamp, last_event varchar(50), last_event_actor varchar(100), requested_state varchar(25) )")
            	.execute();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
	}

}
