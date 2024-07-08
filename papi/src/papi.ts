import Database from "./database";

export default class PAPI {
  static search(username: string) {
    return Database.getPerson(username);
  }

  static optIn(username: string) {
    Database.setPersonActive(username, true);
  }

  static optOut(username: string) {
    Database.setPersonActive(username, false);
  }
}
