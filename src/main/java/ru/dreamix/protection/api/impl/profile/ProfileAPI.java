package ru.dreamix.protection.api.impl.profile;

import ru.dreamix.protection.api.exceptions.ApiException;
import ru.dreamix.protection.api.IApi;
import ru.dreamix.protection.api.impl.profile.models.ProfileInformationModel;
import ru.dreamix.protection.api.bridge.ProfileNativeBridge;
import ru.dreamix.protection.api.impl.profile.objects.RoleObject;
import ru.dreamix.protection.api.impl.profile.objects.SubscriptionObject;

public class ProfileAPI implements IApi<ProfileInformationModel> {
   private ProfileInformationModel profileInformationModel;
   private static ProfileAPI allocatedProfile;

   public static ProfileAPI get() {
      if (allocatedProfile == null) {
         throw new ApiException("profile not allocated.");
      } else {
         return allocatedProfile;
      }
   }

   public static boolean isAllocated() {
      return allocatedProfile != null;
   }

   private static ProfileAPI createNativeProfile() throws UnsatisfiedLinkError {
      return new ProfileAPI(
         new ProfileInformationModel(
            ProfileNativeBridge.getUsername(),
            new SubscriptionObject(ProfileNativeBridge.getSubscriptionTill()),
            ProfileNativeBridge.getUserIdentifier(),
            ProfileNativeBridge.getRole(),
            ProfileNativeBridge.getLaunchedProduct()
         )
      );
   }

   public static ProfileAPI create(String name, int userIdentifier, RoleObject role) {
      if (allocatedProfile != null) {
         return allocatedProfile;
      }

      try {
         return allocatedProfile = createNativeProfile();
      } catch (UnsatisfiedLinkError exception) {
         return allocatedProfile = new ProfileAPI(new ProfileInformationModel(name, new SubscriptionObject("01.01.2048"), userIdentifier, role, ""));
      }
   }

   public static ProfileAPI createWithProduct(String name, int userIdentifier, RoleObject role, String launchedProduct) {
      if (allocatedProfile != null) {
         return allocatedProfile;
      }

      try {
         return allocatedProfile = createNativeProfile();
      } catch (UnsatisfiedLinkError exception) {
         return allocatedProfile = new ProfileAPI(
            new ProfileInformationModel(name, new SubscriptionObject("01.01.2048"), userIdentifier, role, launchedProduct)
         );
      }
   }

   public static void updateProfile(ProfileInformationModel model) {
      if (allocatedProfile != null) {
         allocatedProfile.profileInformationModel = model;
      } else {
         allocatedProfile = new ProfileAPI(model);
      }
   }

   public void setModel(ProfileInformationModel model) {
      this.profileInformationModel = model;
   }

   public ProfileInformationModel getModel() {
      return this.profileInformationModel;
   }

   public ProfileAPI(ProfileInformationModel initialProfileInformationModel) {
      this.profileInformationModel = initialProfileInformationModel;
   }
}
