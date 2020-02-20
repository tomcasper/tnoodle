import React, { Component } from "react";
import SelectCompetition from "./SelectCompetition";
import { connect } from "react-redux";
import { fetchMe, isLogged } from "../api/wca.api";
import { updateMe } from "../redux/ActionCreators";
import Loading from "./Loading";

const mapStateToProps = store => ({
    me: store.me
});

const mapDispatchToProps = {
    updateMe: updateMe
};

const OnlineScrambler = connect(
    mapStateToProps,
    mapDispatchToProps
)(
    class extends Component {
        constructor(props) {
            super(props);
            this.state = {
                me: this.props.me,
                unauthorized: false,
                loading: false
            };
        }

        componentDidMount() {
            if (!isLogged()) {
                return;
            }
            if (this.state.me == null) {
                this.setLoading(true);
                fetchMe()
                    .then(me => {
                        this.handleUpdateMe(me);
                        this.setLoading(false);
                    })
                    .catch(e => {
                        this.setState({ ...this.state, unauthorized: true });
                    });
            }
        }

        setLoading = flag => {
            this.setState({ ...this.state, loading: flag });
        };

        handleUpdateMe = me => {
            this.setState({ ...this.state, me: me });
            this.props.updateMe(me);
        };

        render() {
            if (this.state.unauthorized) {
                return (
                    <div>
                        <h4>There was a problem while fetching information.</h4>
                        <p>Try to log in again.</p>
                    </div>
                );
            }

            if (this.state.loading) {
                return <Loading />;
            }

            if (this.state.me == null) {
                return (
                    <div>
                        <h1>You have to login first</h1>
                    </div>
                );
            }

            return (
                <div className="container">
                    <h1>Welcome, {this.state.me.name}</h1>
                    <SelectCompetition />
                </div>
            );
        }
    }
);

export default OnlineScrambler;