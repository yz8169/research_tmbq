package models
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.jdbc.MySQLProfile
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import com.github.tototoshi.slick.MySQLJodaSupport._
  import org.joda.time.DateTime
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Config.schema ++ Mission.schema ++ Mode.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table Config
   *  @param kind Database column kind SqlType(VARCHAR), PrimaryKey, Length(255,true)
   *  @param value Database column value SqlType(VARCHAR), Length(255,true) */
  case class ConfigRow(kind: String, value: String)
  /** GetResult implicit for fetching ConfigRow objects using plain SQL queries */
  implicit def GetResultConfigRow(implicit e0: GR[String]): GR[ConfigRow] = GR{
    prs => import prs._
    ConfigRow.tupled((<<[String], <<[String]))
  }
  /** Table description of table config. Objects of this class serve as prototypes for rows in queries. */
  class Config(_tableTag: Tag) extends profile.api.Table[ConfigRow](_tableTag, Some("scientific_tmbq"), "config") {
    def * = (kind, value) <> (ConfigRow.tupled, ConfigRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(kind), Rep.Some(value))).shaped.<>({r=>import r._; _1.map(_=> ConfigRow.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column kind SqlType(VARCHAR), PrimaryKey, Length(255,true) */
    val kind: Rep[String] = column[String]("kind", O.PrimaryKey, O.Length(255,varying=true))
    /** Database column value SqlType(VARCHAR), Length(255,true) */
    val value: Rep[String] = column[String]("value", O.Length(255,varying=true))
  }
  /** Collection-like TableQuery object for table Config */
  lazy val Config = new TableQuery(tag => new Config(tag))

  /** Entity class storing rows of table Mission
   *  @param id Database column id SqlType(INT), AutoInc, PrimaryKey
   *  @param missionName Database column mission_name SqlType(TEXT)
   *  @param kind Database column kind SqlType(VARCHAR), Length(255,true)
   *  @param startTime Database column start_time SqlType(DATETIME)
   *  @param endTime Database column end_time SqlType(DATETIME), Default(None)
   *  @param state Database column state SqlType(VARCHAR), Length(255,true)
   *  @param cpu Database column cpu SqlType(INT)
   *  @param email Database column email SqlType(VARCHAR), Length(255,true), Default(None) */
  case class MissionRow(id: Int, missionName: String, kind: String, startTime: DateTime, endTime: Option[DateTime] = None, state: String, cpu: Int, email: Option[String] = None)
  /** GetResult implicit for fetching MissionRow objects using plain SQL queries */
  implicit def GetResultMissionRow(implicit e0: GR[Int], e1: GR[String], e2: GR[DateTime], e3: GR[Option[DateTime]], e4: GR[Option[String]]): GR[MissionRow] = GR{
    prs => import prs._
    MissionRow.tupled((<<[Int], <<[String], <<[String], <<[DateTime], <<?[DateTime], <<[String], <<[Int], <<?[String]))
  }
  /** Table description of table mission. Objects of this class serve as prototypes for rows in queries. */
  class Mission(_tableTag: Tag) extends profile.api.Table[MissionRow](_tableTag, Some("scientific_tmbq"), "mission") {
    def * = (id, missionName, kind, startTime, endTime, state, cpu, email) <> (MissionRow.tupled, MissionRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(missionName), Rep.Some(kind), Rep.Some(startTime), endTime, Rep.Some(state), Rep.Some(cpu), email)).shaped.<>({r=>import r._; _1.map(_=> MissionRow.tupled((_1.get, _2.get, _3.get, _4.get, _5, _6.get, _7.get, _8)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column mission_name SqlType(TEXT) */
    val missionName: Rep[String] = column[String]("mission_name")
    /** Database column kind SqlType(VARCHAR), Length(255,true) */
    val kind: Rep[String] = column[String]("kind", O.Length(255,varying=true))
    /** Database column start_time SqlType(DATETIME) */
    val startTime: Rep[DateTime] = column[DateTime]("start_time")
    /** Database column end_time SqlType(DATETIME), Default(None) */
    val endTime: Rep[Option[DateTime]] = column[Option[DateTime]]("end_time", O.Default(None))
    /** Database column state SqlType(VARCHAR), Length(255,true) */
    val state: Rep[String] = column[String]("state", O.Length(255,varying=true))
    /** Database column cpu SqlType(INT) */
    val cpu: Rep[Int] = column[Int]("cpu")
    /** Database column email SqlType(VARCHAR), Length(255,true), Default(None) */
    val email: Rep[Option[String]] = column[Option[String]]("email", O.Length(255,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table Mission */
  lazy val Mission = new TableQuery(tag => new Mission(tag))

  /** Entity class storing rows of table Mode
   *  @param id Database column id SqlType(INT), AutoInc, PrimaryKey
   *  @param test Database column test SqlType(VARCHAR), Length(255,true) */
  case class ModeRow(id: Int, test: String)
  /** GetResult implicit for fetching ModeRow objects using plain SQL queries */
  implicit def GetResultModeRow(implicit e0: GR[Int], e1: GR[String]): GR[ModeRow] = GR{
    prs => import prs._
    ModeRow.tupled((<<[Int], <<[String]))
  }
  /** Table description of table mode. Objects of this class serve as prototypes for rows in queries. */
  class Mode(_tableTag: Tag) extends profile.api.Table[ModeRow](_tableTag, Some("scientific_tmbq"), "mode") {
    def * = (id, test) <> (ModeRow.tupled, ModeRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(test))).shaped.<>({r=>import r._; _1.map(_=> ModeRow.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column test SqlType(VARCHAR), Length(255,true) */
    val test: Rep[String] = column[String]("test", O.Length(255,varying=true))
  }
  /** Collection-like TableQuery object for table Mode */
  lazy val Mode = new TableQuery(tag => new Mode(tag))
}
