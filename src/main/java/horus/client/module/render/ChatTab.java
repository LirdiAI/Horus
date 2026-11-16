package horus.client.module.render;

public enum ChatTab {
   ALL("Все чаты", "Все"),
   PM("Личные [ЛС]", "ЛС"),
   CLAN("Клан", "Клан"),
   STAFF("Staff", "Staff"),
   SERVER("Сервер", "Сервер");

   private final String fullName;
   private final String shortName;

   ChatTab(String fullName, String shortName) {
      this.fullName = fullName;
      this.shortName = shortName;
   }

   public String getFullName() {
      return this.fullName;
   }

   public String getShortName() {
      return this.shortName;
   }
}
