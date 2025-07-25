import { UserAccount } from './user-account';
import { QuestionType } from './question-type';
import { Choice } from './choice';
import { Part } from './part';

// File: question.ts

export class Question {
  id?: number;
  createdDate?: string;
  lastModifiedDate?: string;
  createdBy?: UserAccount;
  lastModifiedBy?: UserAccount;
  questionText: string;
  questionType?: QuestionType;
  choices: Choice[];
  part?: Part;
  point: number;      // GIỮ point
  isSelected?: boolean;
  deleted?: boolean;
  userAnswer?: string; // <-- Thêm dòng này (cho phép undefined cũng được)
  constructor(
    questionText: string,
    choices: Choice[],
    point: number,
  ) {
    this.questionText = questionText;
    this.choices = choices;
    this.point = point;
  }
}
