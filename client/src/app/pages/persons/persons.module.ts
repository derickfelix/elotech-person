import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { SharedModule } from "src/app/shared/shared.module";
import { ContactsModal } from "./contacts-modal/contacts.modal";
import { PersonsPage } from "./persons.page";

const routes: Routes = [
  {
    path: '',
    component: PersonsPage
  }
];

@NgModule({
  declarations: [PersonsPage, ContactsModal],
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    SharedModule
  ]
})
export class PersonsPageModule { }