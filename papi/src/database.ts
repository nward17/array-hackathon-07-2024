import * as SQLite from "better-sqlite3";

export default class Database {
  private static readonly DB_NAME = "papi.db";

  static getPerson(username: string) {
    const db = new SQLite(Database.DB_NAME);
    const stmt = db.prepare(
      "SELECT * FROM people WHERE username = ? AND active = ?"
    );
    const user = stmt.get(username, 1);
    db.close();
    return user;
  }

  static setPersonActive(username: string, active: boolean) {
    const db = new SQLite(Database.DB_NAME);
    const stmt = db.prepare("UPDATE people SET active = ? WHERE username = ?");
    stmt.run(active ? 1 : 0, username);
    db.close();
  }
}
