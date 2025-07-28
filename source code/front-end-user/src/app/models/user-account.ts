import { UserProfile } from './user-profile';
import { Intake } from './intake';
import { Role } from './role';

export class UserAccount {
  id?: number;
  username: string;
  birthDate?: Date; // ✅ Thêm ngày sinh (thay cho email)
  enabled?: boolean;
  deleted?: boolean;
  createdDate?: Date;
  profile: UserProfile;
  intake?: Intake;
  password?: string;
  roleId?: number;
  roles: Role[];

  constructor(
    username: string,
    profile: UserProfile,
    birthDate?: Date,
    roleId?: number,
    password?: string
  ) {
    this.username = username;
    this.profile = profile;
    this.birthDate = birthDate;
    this.roleId = roleId;
    this.password = password;
  }
}
