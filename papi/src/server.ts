import * as Database from "better-sqlite3";
import * as express from "express";

const DB_NAME = "papi.db";

function search(username: string) {
  const db = new Database(DB_NAME);
  const stmt = db.prepare(
    "SELECT * FROM people WHERE username = ? AND active = ?"
  );
  const user = stmt.get(username, 1);
  db.close();
  return user;
}

function optIn(username: string) {
  const db = new Database(DB_NAME);
  const stmt = db.prepare("UPDATE people SET active = ? WHERE username = ?");
  stmt.run(1, username);
  db.close();
}

function optOut(username: string) {
  const db = new Database(DB_NAME);
  const stmt = db.prepare("UPDATE people SET active = ? WHERE username = ?");
  stmt.run(0, username);
  db.close();
}

const app = express();

app.get("/search", function (req, res) {
  const username: string = req.query.username as string;
  const person = search(username);
  res.send(JSON.stringify(person));
});

app.get("/optIn", function (req, res) {
  const username: string = req.query.username as string;
  optIn(username);
  res.send(`You have opted in ${username}.`);
});

app.get("/optOut", function (req, res) {
  const username: string = req.query.username as string;
  optOut(username);
  res.send(`You have opted out ${username}.`);
});

app.listen(3000);
