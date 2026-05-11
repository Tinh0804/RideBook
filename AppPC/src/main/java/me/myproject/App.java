package me.myproject;

import java.io.IOException;

import me.myproject.GUI.DangNhapView;
import me.myproject.GUI.TrangChuDriverView;
import me.myproject.GUI.TrangChuUserView;
import me.myproject.MODEL.TaiKhoan;
import me.myproject.Utilities.AuthManager;

public class App{
  public static void main(String[] args) throws IOException {
      TaiKhoan restored = AuthManager.tryRestoreSession();
      if (restored != null) {
          if ("DRIVER".equalsIgnoreCase(restored.getID_VaiTro())) {
              new TrangChuDriverView(restored);
          } else {
              new TrangChuUserView(restored);
          }
          return;
      }
      new DangNhapView();
    }
}
