package server.database.schedule;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import common.models.Schedule;
import server.database.ObjectHandler;

/**
 * This class is responsible for all the schedule object interactions with the database.
 *
 * @author Perdana Bailey
 * @author Kevin Huynh
 * @author Jamie Martin
 */
public class ScheduleHandler implements ObjectHandler<Schedule> {
    // This is the database connection
    Connection connection;

    // This is the mock "database" used for testing
    List<Schedule> mockDB = new ArrayList<Schedule>();
    int mockDBNum = 0;

    public ScheduleHandler(Connection connection) {
        this.connection = connection;

    }

    /**
     * Get schedule from schedule ID.
     *
     * @param scheduleId: the id of the schedule.
     * @return Optional<Schedule>: this returns the billboard schedule or an optional empty value.
     * @throws SQLException: this exception is thrown when there is an issue fetching data from the database.
     */
    public Optional<Schedule> get(int scheduleId) throws SQLException {
        // Check that it's not in testing mode
        if (this.connection != null) {
            // Initialise return value
            Optional<Schedule> returnSchedule = Optional.empty();

            // Attempt to query the database
            Statement sqlStatement = connection.createStatement();

            // Create a query that selects schedule based on the id and execute the query
            ResultSet result = sqlStatement.executeQuery("SELECT * FROM SCHEDULES WHERE id = " + scheduleId);

            // Use the result of the database query to create the schedule object and save it
            while (result.next()) {
                returnSchedule = Optional.of(Schedule.fromSQL(result));
            }

            // Clean up query
            sqlStatement.close();

            return returnSchedule;
        } else {
            // Loop through and find the schedule with the requested ID or return an optional empty value
            for (Schedule s : this.mockDB) {
                if (s.id == scheduleId) {
                    return Optional.of(s);
                }
            }

            return Optional.empty();
        }
    }

    /**
     * Get schedule from billboard name.
     *
     * @param billboardName: the name of the billboard.
     * @return Optional<Schedule>: this returns the billboard schedule or an optional empty value.
     * @throws SQLException: this exception is thrown when there is an issue fetching data from the database.
     */
    public Optional<Schedule> get(String billboardName) throws SQLException {
        // Check that it's not in testing mode
        if (this.connection != null) {
            // Initialise return value
            Optional<Schedule> returnSchedule = Optional.empty();

            // Attempt to query the database
            Statement sqlStatement = connection.createStatement();

            // Create a query that selects schedule based on the id and execute the query
            ResultSet result = sqlStatement.executeQuery("SELECT * FROM SCHEDULES WHERE billboardName = " + billboardName);

            // Use the result of the database query to create the schedule object and save it
            while (result.next()) {
                returnSchedule = Optional.of(Schedule.fromSQL(result));
            }

            // Clean up query
            sqlStatement.close();

            return returnSchedule;
        } else {
            // Loop through and find the schedule with the requested ID or return an optional empty value
            for (Schedule s : this.mockDB) {
                if (s.billboardName == billboardName) {
                    return Optional.of(s);
                }
            }

            return Optional.empty();
        }
    }

    /**
     * List all the schedules in the database.
     *
     * @return List<Schedule>: this returns the list of schedules requested or a optional empty value.
     * @throws SQLException: this exception returns when there is an issue fetching data from the database.
     */
    public List<Schedule> getAll() throws SQLException {
        // Schedule list to be returned
        List<Schedule> schedules = new ArrayList<>();

        // Check that it's not in testing mode
        if (this.connection != null) {
            // Attempt to query the database
            Statement sqlStatement = connection.createStatement();

            // Create a query that selects all schedules and execute the query
            ResultSet result = sqlStatement.executeQuery("SELECT * FROM SCHEDULES");

            // Use the result of the database query to create the schedule object and add it to the returned list
            while (result.next()) {
                schedules.add(Schedule.fromSQL(result));
            }

            // Clean up query
            sqlStatement.close();
        } else {
            schedules = this.mockDB;
        }

        return schedules;
    }

    /**
     * Insert a schedule into database.
     *
     * @param schedule: this is the requested schedule to insert.
     * @throws SQLException: this exception returns when there is an issue sending data to the database
     */
    public void insert(Schedule schedule) throws SQLException {
        // Check that it's not in testing mode
        if (this.connection != null) {
            // Attempt to query the database
            Statement sqlStatement = connection.createStatement();

            // Create a query that adds the schedule and execute the query
            String query = "INSERT INTO SCHEDULES" +
                "(billboardName, startTime, duration, `interval`)" +
                "VALUES('" + schedule.billboardName + "', (timestamp '" +
                java.sql.Timestamp.from(schedule.startTime) + "')," + schedule.duration + "," + schedule.interval +
                ")";
            sqlStatement.executeUpdate(query);

            // Clean up query
            sqlStatement.close();
        } else {
            // Emulate auto increment ID
            schedule.id = mockDBNum;
            this.mockDB.add(schedule);
            mockDBNum++;
        }
    }

    /**
     * Update a schedule in the database.
     *
     * @param schedule: this is the requested schedule to update.
     * @throws SQLException: this exception returns when there is an issue sending data to the database.
     */
    public void update(Schedule schedule) throws SQLException {
        // Check that it's not in testing mode
        if (this.connection != null) {
            // Attempt to query the database
            Statement sqlStatement = connection.createStatement();

            // Create a query that updates the user permissions and execute the query
            String query = "UPDATE SCHEDULES " +
                "SET startTime = (timestamp '" + java.sql.Timestamp.from(schedule.startTime) +
                "'), duration = " + schedule.duration +
                ", `interval` = " + schedule.interval +
                " WHERE id = " + schedule.id;
            sqlStatement.executeUpdate(query);

            // Clean up query
            sqlStatement.close();
        } else {
            // Loop through mock database and find the schedule to update, then update it
            for (Schedule mockSchedule : this.mockDB) {
                if (mockSchedule.id == schedule.id) {
                    mockSchedule = schedule;
                }
            }
        }
    }


    /**
     * Delete a schedule from the database.
     * @param schedule: the content of the schedule.
     * @throws SQLException: this exception is thrown when there is an issue deleting data from the database.
     */
    public void delete(Schedule schedule) throws Exception {
        // Check that it's not in testing mode
        if (this.connection != null) {
            // Attempt to query the database
            Statement sqlStatement = connection.createStatement();

            // Create a query that deletes the schedule and executes the query
            sqlStatement.executeUpdate("DELETE FROM SCHEDULES WHERE schedules.id = " + schedule.id);

            // Clean up query
            sqlStatement.close();
        } else {
            this.mockDB.remove(schedule);
        }
    }

    /**
     * clears all entries in database for unit test cleanup
     * @throws Exception
     */
    public void deleteAll() throws Exception {
        if (this.connection != null) {
            // Attempt to query the database
            Statement sqlStatement = connection.createStatement();
            // Create a query that deletes the billboard and executes the query
            String query = "TRUNCATE TABLE SCHEDULES";
            sqlStatement.executeUpdate(query);

            // Cleans up query
            sqlStatement.close();
        } else {
            // Delete all billboards
            mockDB.clear();
        }
    }
}



