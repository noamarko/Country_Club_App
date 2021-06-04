import './App.css';
import React, { useState } from 'react';
import Home from './components/Home/Home';
import RegistrationForm from './components/RegistrationForm/RegistrationForm';
import LoginForm from './components/LoginForm/LoginForm';
import Header from './components/HeaderComponent/HeaderComponent';
import AlertComponent from './components/AlertComponent/AlertComponent'

import {
  BrowserRouter as Router,
  Switch,
  Route
} from "react-router-dom";

function App() {

  const [errorMessage, updateErrorMessage] = useState(null);
  const [title, updateTitle] = useState(null);
  const [userRegister, createUser] = useState({email:'', role:'', username:'', avatar:''});
  const [userBoundary, setUserBoundary] = useState({userId:{space:'', email:''}, role:'', username:'', avatar:''});

  return ( <Router>
    <div className="App">
      <Header title={title}/>
        <div className="container d-flex align-items-center flex-column">
          <Switch>
            <Route path="/" exact={true}>
              <LoginForm showError={updateErrorMessage} updateTitle={updateTitle} registrationDetails={userRegister} loginDetails={setUserBoundary}/>
            </Route>
            <Route path="/register">
              <RegistrationForm showError={updateErrorMessage} updateTitle={updateTitle} newUser={createUser}/>
            </Route>
            <Route path="/login">
              <LoginForm showError={updateErrorMessage} updateTitle={updateTitle} registrationDetails={userRegister} loginDetails={setUserBoundary} >
                {console.log(userBoundary)}
              </LoginForm> 
            </Route>
            <Route path="/home">
              <Home showError={updateErrorMessage} updateTitle={updateTitle} loginDetails={userBoundary}/> 
            </Route>
          </Switch>
          <AlertComponent errorMessage={errorMessage} hideError={updateErrorMessage}/>
        </div>
    </div>
    </Router>
  );
  
}

export default App;
