import {UserRole} from './user-role';
import {UserProfile} from './user-profile';
import {Intake} from './intake';

export class UserAccount {
  id?: number;
  username: string;
  email: string;
  enabled?: boolean;
  deleted?: boolean;
  createdDate?: Date;
  roles?: any[]; // hoặc UserRole[] nếu có
  profile: any; // hoặc UserProfile
  intake?: any; // hoặc Intake
  password?: string;
  roleId?: number; // THÊM DÒNG NÀY!

  constructor(
    username: string,
    email: string,
    profile: any,
    roleId?: number,
    password?: string
  ) {
    this.username = username;
    this.email = email;
    this.profile = profile;
    this.roleId = roleId;      // GÁN THÊM DÒNG NÀY!
    this.password = password;
  }
}

