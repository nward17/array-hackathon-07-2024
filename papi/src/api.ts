import * as express from "express";
import PAPI from "./papi";

function main() {
  const app = express();
  const port = 3000;

  app.get("/search", function (req, res) {
    const username: string = req.query.username as string;
    const person = PAPI.search(username);
    res.send(JSON.stringify(person));
  });

  app.get("/optIn", function (req, res) {
    const username: string = req.query.username as string;
    PAPI.optIn(username);
    res.send(`You have opted in ${username}.`);
  });

  app.get("/optOut", function (req, res) {
    const username: string = req.query.username as string;
    PAPI.optOut(username);
    res.send(`You have opted out ${username}.`);
  });

  app.listen(3000, () => {
    console.log(`PAPI is now listening on port ${port}.`);
  });
}

main();
