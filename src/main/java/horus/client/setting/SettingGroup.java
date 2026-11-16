package horus.client.setting;

import horus.client.module.Module;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class SettingGroup {
   public final String strVal;
   public final Object object;
   public final List<Setting<?>> list;
   private static final Map<Module, SettingGroup.DataRecord> map = new IdentityHashMap<>();

   private SettingGroup(String local614_1, Object local614_2, List<Setting<?>> local614_3) {
      this.strVal = local614_1;
      this.object = local614_2;
      this.list = local614_3;
   }

   public boolean checkState() {
      return this.strVal != null;
   }

   public static List<SettingGroup> getList(Module module) {
      List<Setting<?>> settings = module.getSettings();
      SettingGroup.DataRecord local617_2 = map.get(module);
      int local617_3 = settings.size();
      Setting<?> local617_4 = local617_3 == 0 ? null : settings.get(local617_3 - 1);
      if (local617_2 == null || local617_2.size != local617_3 || local617_2.last != local617_4) {
         local617_2 = new SettingGroup.DataRecord(List.copyOf(getList2(module)), local617_3, local617_4);
         map.put(module, local617_2);
      }

      return local617_2.sections;
   }

   public static boolean matchesCriteria(List<SettingGroup> local618_0) {
      for (SettingGroup settingGroup : local618_0) {
         for (Setting<?> local618_4 : settingGroup.list) {
            if (local618_4.isVisible()) {
               return true;
            }
         }
      }

      return false;
   }

   public static List<SettingGroup> getList2(Module module) {
      List<SettingGroup> local619_1 = new ArrayList<>();
      String local619_2 = null;
      Object module2 = module;
      List<Setting<?>> local619_4 = new ArrayList<>();

      for (Setting<?> local619_6 : module.getSettings()) {
         if (local619_6 instanceof SectionSetting sectionSetting) {
            if (!local619_4.isEmpty() || local619_2 != null) {
               local619_1.add(new SettingGroup(local619_2, module2, local619_4));
            }

            local619_2 = sectionSetting.name;
            module2 = sectionSetting;
            local619_4 = new ArrayList<>();
         } else {
            local619_4.add(local619_6);
         }
      }

      if (!local619_4.isEmpty() || local619_2 != null) {
         local619_1.add(new SettingGroup(local619_2, module2, local619_4));
      }

      return local619_1;
   }

   private record DataRecord(List<SettingGroup> sections, int size, Setting<?> last) {
   }
}
