import { BrowserModule } from "@angular/platform-browser";
import { NgModule } from "@angular/core";

import { AppComponent } from "./app.component";
import { ConnectionsModule } from "../../projects/beetle-lib/src/connections/connections.module";
import { DataservicesModule } from "../../projects/beetle-lib/src/dataservices/dataservices.module";
import { AppRoutingModule } from "./app-routing.module";

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    ConnectionsModule,
    DataservicesModule,
    AppRoutingModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
