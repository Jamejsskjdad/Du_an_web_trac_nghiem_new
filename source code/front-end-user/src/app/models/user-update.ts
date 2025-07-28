import { UserProfile } from './user-profile';

export class UserUpdate {
  username: string;
  password?: string;
  profile: UserProfile;
  birthDate?: Date; // ✅ Thêm ngày sinh, kiểu ISO string

  constructor(
    username: string,
    profile: UserProfile,
    birthDate?: Date,
    password?: string
  ) {
    this.username = username;
    this.profile = profile;
    this.birthDate = birthDate;
    this.password = password;
  }
}
