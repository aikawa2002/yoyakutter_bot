package jp.co.jrits.yoyakutter_bot.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javax.sql.DataSource;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class SQLExecuter {
	DataSource source;

	public SQLExecuter() throws IOException {
			this.source = getDataSource();
	}

	public Collection<User> selectUser(String user) throws Exception {
		Connection con = source.getConnection();
		PreparedStatement pstmt = con.prepareStatement("SELECT id,name FROM user where name = ?");
		pstmt.setString(1, user);
		ResultSet rset = pstmt.executeQuery();
		Statement state = con.createStatement();
        final Collection<User> names = new LinkedList<User>();
		while(rset.next()) {
            names.add(new User(rset.getInt(1),rset.getString(2)));
		}
		return names;

	}

	public Collection<Resource> selectResource(String rental) throws Exception {
		Connection con = source.getConnection();
		PreparedStatement pstmt = con.prepareStatement("SELECT resource.id,resource.name,type_master.name FROM resource join type_master on resource.type_id = type_master.id where resource.name like ? or type_master.name like ?");
		pstmt.setString(1, "%" + rental + "%");
		pstmt.setString(2, "%" + rental + "%");
		System.out.println("selectResource:" + rental);
		ResultSet rset = pstmt.executeQuery();
        final Collection<Resource> names = new LinkedList<Resource>();
		while(rset.next()) {
            names.add(new Resource(rset.getInt(1),rset.getString(2),rset.getString(3)));
		}
		return names;

	}

	public Collection<Resource> selectUsingdevice(String rental, String date) throws Exception  {
		Connection con = source.getConnection();
		PreparedStatement pstmt = con.prepareStatement("SELECT a.id,a.name FROM resource a join type_master on a.type_id = type_master.id and (a.name like ? or type_master.name like ?) where not exists (SELECT * FROM plan_result c where c.resource_id = a.id and DATE_FORMAT( c.use_start  , '%Y-%m-%d') <= str_to_date( ? , '%Y-%m-%d') and c.finish is null)");
		pstmt.setString(1, "%" + rental + "%");
		pstmt.setString(2, "%" + rental + "%");
		pstmt.setString(3, date);
		System.out.println("selectUsingdevice:" + rental);
		ResultSet rset = pstmt.executeQuery();
        final Collection<Resource> names = new LinkedList<Resource>();
		while(rset.next()) {
            names.add(new Resource(rset.getInt(1),rset.getString(2),null));
		}
		return names;
	}

	public Collection<Resource> selectResource(String bihinNumber,String rental) throws Exception {
		Connection con = source.getConnection();
		PreparedStatement pstmt = con.prepareStatement("SELECT resource.id,resource.name FROM resource where resource.name like ? and resource.name like ?");
		pstmt.setString(1, "%" + bihinNumber + "%");
		pstmt.setString(2, "%" + rental + "%");
		System.out.println("selectResource:" + rental +":" + bihinNumber);
		ResultSet rset = pstmt.executeQuery();
        final Collection<Resource> names = new LinkedList<Resource>();
		while(rset.next()) {
	        System.out.println(rental +":" + bihinNumber);
            names.add(new Resource(rset.getInt(1),rset.getString(2),null));
		}
		return names;

	}

    public int insertResource(String rentalName,String category) throws Exception {
        Connection con = source.getConnection();
        PreparedStatement pstmt = con.prepareStatement("insert into resource(no,name,type_id,mac_address,isTakeDisable,entry) select max(cast(no as signed)) + 1,?,?,'',0,'' from resource limit 1;");
        pstmt.setString(1, rentalName);
        pstmt.setString(2, category);
        int rset = pstmt.executeUpdate();

        return rset;
    }

	public Collection<PlanResult> selectPlanResult() throws Exception {
		Connection con = source.getConnection();
		Statement state = con.createStatement();
		ResultSet rset = state.executeQuery("SELECT b.id,b.name,c.name,DATE_FORMAT( a.use_start  , '%Y-%m-%d %H:%i'),a.finish FROM plan_result a join resource b on a.resource_id = b.id join user c on a.user_id = c.id");
        final Collection<PlanResult> names = new LinkedList<PlanResult>();
		while(rset.next()) {
            names.add(new PlanResult(rset.getString(1),rset.getString(2),rset.getString(3),rset.getString(4),rset.getString(5)));
		}
		return names;

	}

	public Collection<PlanResult> selectPlanResult(String sql) throws Exception {
		Connection con = source.getConnection();
		Statement state = con.createStatement();
		ResultSet rset = state.executeQuery(sql);
        final Collection<PlanResult> names = new LinkedList<PlanResult>();
		while(rset.next()) {
            names.add(new PlanResult(rset.getString(1),rset.getString(2),rset.getString(3),rset.getString(4),rset.getString(5)));
		}
		return names;

	}

	public Collection<PlanResult> selectPlanResult(String category,String mention,List<String> sysDate) throws Exception {
		if (sysDate.size() == 1) {
			return selectPlanResult(category,mention,sysDate.get(0));
		}
		Connection con = source.getConnection();
		PreparedStatement pstmt = con.prepareStatement("SELECT b.id,b.name,c.name,DATE_FORMAT( a.use_start  , '%Y-%m-%d %H:%i'),a.finish FROM plan_result a join resource b on a.resource_id = b.id join user c on a.user_id = c.id and c.name = ? where DATE_FORMAT( a.use_start  , '%Y-%m-%d') between str_to_date( ? , '%Y-%m-%d') and str_to_date( ? , '%Y-%m-%d')");
		pstmt.setString(1, mention);
		pstmt.setString(2, sysDate.get(0));
		pstmt.setString(3, sysDate.get(1));
		ResultSet rset = pstmt.executeQuery();

        final Collection<PlanResult> names = new LinkedList<PlanResult>();
		while(rset.next()) {
            names.add(new PlanResult(rset.getString(1),rset.getString(2),rset.getString(3),rset.getString(4),rset.getString(5)));
		}
		return names;

	}

	public Collection<PlanResult> selectRentalResource(String category,String mention) throws Exception {
		Connection con = source.getConnection();
		PreparedStatement pstmt = con.prepareStatement("SELECT b.id,b.name,c.name,DATE_FORMAT( a.use_start  , '%Y-%m-%d %H:%i'),a.finish FROM plan_result a join resource b on a.finish is null and a.resource_id = b.id join user c on a.user_id = c.id and c.name = ? where b.name like ?");
		pstmt.setString(1, mention);
		pstmt.setString(2, "%" + category + "%");
		ResultSet rset = pstmt.executeQuery();

        final Collection<PlanResult> names = new LinkedList<PlanResult>();
		while(rset.next()) {
            names.add(new PlanResult(rset.getString(1),rset.getString(2),rset.getString(3),rset.getString(4),rset.getString(5)));
		}
		return names;

	}

	public Collection<PlanResult> selectRentalResource(String mention) throws Exception {
		Connection con = source.getConnection();
		PreparedStatement pstmt = con.prepareStatement("SELECT b.id,b.name,c.name,DATE_FORMAT( a.use_start  , '%Y-%m-%d %H:%i'),a.finish FROM plan_result a join resource b on a.finish is null and a.resource_id = b.id join user c on a.user_id = c.id and c.name = ?" );
		pstmt.setString(1, mention);
		ResultSet rset = pstmt.executeQuery();

        final Collection<PlanResult> names = new LinkedList<PlanResult>();
		while(rset.next()) {
            names.add(new PlanResult(rset.getString(1),rset.getString(2),rset.getString(3),rset.getString(4),rset.getString(5)));
		}
		return names;

	}

	public Collection<PlanResult> selectPlanResult(String category,String mention,String sysDate) throws Exception {
		Connection con = source.getConnection();
		PreparedStatement pstmt = con.prepareStatement("SELECT b.id,b.name,c.name,DATE_FORMAT( a.use_start  , '%Y-%m-%d %H:%i'),a.finish FROM plan_result a join resource b on a.resource_id = b.id join user c on a.user_id = c.id and c.name = ? where DATE_FORMAT( a.use_start  , '%Y-%m-%d')= str_to_date( ? , '%Y-%m-%d')");
		pstmt.setString(1, mention);
		pstmt.setString(2, sysDate);
		ResultSet rset = pstmt.executeQuery();

        final Collection<PlanResult> names = new LinkedList<PlanResult>();
		while(rset.next()) {
            names.add(new PlanResult(rset.getString(1),rset.getString(2),rset.getString(3),rset.getString(4),rset.getString(5)));
		}
		return names;

	}

	public int insertPlanResult(String rental,String mention,String useFrom,String reserveFrom,String reserveTo) throws Exception {
		Connection con = source.getConnection();
		PreparedStatement pstmt = con.prepareStatement("insert into plan_result(resource_id,user_id,use_start,reserve_date,reserve_end) values (?,?,str_to_date( ? , '%Y-%m-%d %H:%i:%S'),str_to_date( ? , '%Y-%m-%d %H:%i:%S'),str_to_date( ? , '%Y-%m-%d %H:%i:%S'))");
		pstmt.setString(1, rental);
		pstmt.setString(2, mention);
		pstmt.setString(3, useFrom);
		pstmt.setString(4, reserveFrom);
		pstmt.setString(5, reserveTo);
		int rset = pstmt.executeUpdate();

		return rset;
	}

	public int updatePlanResult(String rental,String mention,String finish) throws Exception {
		Connection con = source.getConnection();
		System.out.println("resourceid="+rental+":useid="+mention);
		PreparedStatement pstmt = con.prepareStatement("update plan_result set finish = str_to_date( ? , '%Y-%m-%d %H:%i:%S') where resource_id = ? and user_id = ? and finish is null");
		pstmt.setString(1, finish);
		pstmt.setString(2, rental);
		pstmt.setString(3, mention);
		int rset = pstmt.executeUpdate();

		return rset;
	}

	private DataSource getDataSource() throws IOException {
        String url = ResourceBundle.getBundle("credentials").getString("MYSQL_DB_URL");
        String user = ResourceBundle.getBundle("credentials").getString("MYSQL_DB_USERNAME");
        String password = ResourceBundle.getBundle("credentials").getString("MYSQL_DB_PASSWORD");

        MysqlDataSource mysqlDS = null;

        mysqlDS = new MysqlConnectionPoolDataSource();
		mysqlDS.setURL(url);
		mysqlDS.setUser(user);
		mysqlDS.setPassword(password);
		return mysqlDS;
	}

	public class Resource {
		private int id;
		private String resourceName;
		private String typeName;

		Resource(int id, String resourceName, String typeName) {
			this.id=id;
			this.resourceName=resourceName;
			this.typeName=typeName;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getResourceName() {
			return resourceName;
		}

		public void setResourceName(String resourceName) {
			this.resourceName = resourceName;
		}

		public String getTypeName() {
			return typeName;
		}

		public void setTypeName(String typeName) {
			this.typeName = typeName;
		}
	}

	public class PlanResult {
		private String resourceId;
		private String resourceName;
		private String userName;
		private String startTime;
		private String finishTime;

		PlanResult(String resourceId,String resourceName,String userName,String startTime,String finishTime) {
			this.resourceId=resourceId;
			this.resourceName=resourceName;
			this.userName=userName;
			this.startTime=startTime;
			this.finishTime=finishTime;
		}

		public String getResourceId() {
			return resourceId;
		}

		public void setResourceId(String resourceId) {
			this.resourceId = resourceId;
		}

		public String getResourceName() {
			return resourceName;
		}

		public void setResourceName(String resourceName) {
			this.resourceName = resourceName;
		}

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

		public String getStartTime() {
			return startTime;
		}

		public void setStartTime(String startTime) {
			this.startTime = startTime;
		}

		public String getFinishTime() {
			return finishTime;
		}

		public void setFinishTime(String finishTime) {
			this.finishTime = finishTime;
		}


	}

	public class User {
		private int id;
		private String name;

		User(int id, String name) {
			this.id=id;
			this.name=name;
		}

		public int getId() {
			return id;
		}


		public void setId(int id) {
			this.id = id;
		}


		public String getName() {
			return name;
		}


		public void setName(String name) {
			this.name = name;
		}



	}
}
